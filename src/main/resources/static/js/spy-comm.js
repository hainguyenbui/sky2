(function () {
    const root = document.getElementById("spy-comm-root");
    if (!root) return;

    const game = root.dataset.game;
    const deviceId = root.dataset.deviceId;
    const clientInstanceId = Date.now().toString(36) + "-" + Math.random().toString(36).slice(2, 8);
    const myName = root.dataset.playerName || "Ẩn danh";
    const topic = "/topic/comm/" + game + "/" + deviceId;
    const volumeStorageKey = "spy-comm-volume-" + game + "-" + deviceId;
    const micEnabledStorageKey = "spy-comm-mic-enabled-" + game + "-" + deviceId;
    const listenMutedStorageKey = "spy-comm-listen-muted-" + game + "-" + deviceId;

    let state = null;
    let ws = null;
    let stompConnected = false;
    let currentChannel = "public";
    let localStream = null;
    let micEnabled = false;
    let unreadPopupVisible = false;
    let lastReadySignalAt = 0;
    let signalRetryTimer = 0;
    let readySeq = 0;
    let voiceStarting = false;
    let listenMuted = false;
    let voiceStatusBase = "Voice nghe đang bật. Mic đang tắt.";
    const remoteInstanceByDevice = new Map();

    const peerMap = new Map();
    const audioMap = new Map();
    const meterMap = new Map();
    const chatHistory = { public: [], dead: [] };
    const volumePrefs = loadVolumePrefs();
    const persistedMicEnabled = loadMicEnabledPreference();
    listenMuted = loadListenMutedPreference();

    root.innerHTML = [
        '<div class="card" style="display:flex;flex-direction:column;gap:10px;">',
        '<h3 style="margin:0 0 4px 0;text-align:center;">Chat & Voice</h3>',
        '<div id="commPolicyText" style="font-size:0.82rem;color:var(--text-muted);text-align:center;"></div>',
        '<div id="commParticipants" style="font-size:0.8rem;display:flex;flex-wrap:wrap;gap:6px;"></div>',
        '<div id="commControlRow" style="display:flex;gap:8px;align-items:center;">',
        '<select id="commChannelSelect" class="input-text" style="flex:1;"></select>',
        '<button id="commVoiceBtn" class="btn btn-primary" style="width:auto;padding:8px 14px;">Bật Voice</button>',
        '<button id="commListenBtn" class="btn btn-primary" style="width:auto;padding:8px 14px;">Tắt âm</button>',
        '</div>',
        '<div id="commVoiceStatus" style="font-size:0.78rem;color:var(--text-muted);"></div>',
        '<div id="commVolumeList" style="display:flex;flex-direction:column;gap:6px;"></div>',
        '<div id="commMessages" style="height:170px;overflow:auto;border:1px solid var(--card-border);border-radius:10px;padding:8px;background:rgba(15,23,42,0.45);font-size:0.82rem;"></div>',
        '<div id="commComposerRow" style="display:flex;gap:8px;">',
        '<input id="commInput" class="input-text" maxlength="220" placeholder="Nhập tin nhắn..." style="flex:1;" />',
        '<button id="commSendBtn" class="btn btn-primary" style="width:auto;padding:8px 14px;">Gửi</button>',
        "</div>",
        "</div>",
        '<button id="commUnreadPopup" type="button" style="position:fixed;right:16px;bottom:18px;z-index:1000;display:none;padding:10px 14px;border-radius:12px;border:1px solid rgba(96,165,250,0.45);background:rgba(15,23,42,0.95);color:#93c5fd;font-weight:700;font-size:0.82rem;box-shadow:0 4px 18px rgba(0,0,0,0.35);">💬 Có tin nhắn mới</button>'
    ].join("");

    const els = {
        policyText: document.getElementById("commPolicyText"),
        participants: document.getElementById("commParticipants"),
        controlRow: document.getElementById("commControlRow"),
        channelSelect: document.getElementById("commChannelSelect"),
        voiceBtn: document.getElementById("commVoiceBtn"),
        listenBtn: document.getElementById("commListenBtn"),
        voiceStatus: document.getElementById("commVoiceStatus"),
        volumeList: document.getElementById("commVolumeList"),
        messages: document.getElementById("commMessages"),
        input: document.getElementById("commInput"),
        sendBtn: document.getElementById("commSendBtn"),
        popup: document.getElementById("commUnreadPopup")
    };
    updateMicButton();
    updateListenButton();
    renderVoiceStatus();

    els.channelSelect.addEventListener("change", function () {
        currentChannel = els.channelSelect.value || "public";
        renderMessages();
        refreshComposerState();
    });

    els.sendBtn.addEventListener("click", sendChat);
    els.input.addEventListener("keydown", function (event) {
        if (event.key === "Enter") sendChat();
    });
    root.addEventListener("click", hideUnreadPopup);
    els.messages.addEventListener("click", hideUnreadPopup);
    els.input.addEventListener("focus", hideUnreadPopup);
    els.popup.addEventListener("click", function () {
        hideUnreadPopup();
        const channels = Array.isArray(state && state.channels) ? state.channels : [];
        if (channels.length > 1 && channels.some(function (c) { return c.key === "public"; })) {
            currentChannel = "public";
            els.channelSelect.value = "public";
            refreshComposerState();
            renderMessages();
        }
        els.messages.scrollIntoView({ behavior: "smooth", block: "center" });
    });

    els.voiceBtn.addEventListener("click", async function () {
        if (!micEnabled) {
            await startVoice(false);
            return;
        }
        stopVoice(true);
    });
    els.listenBtn.addEventListener("click", function () {
        listenMuted = !listenMuted;
        saveListenMutedPreference(listenMuted);
        updateListenButton();
        applyListenMuteToAll();
        renderVoiceStatus();
        Array.from(els.volumeList.querySelectorAll("input[data-volume-device]")).forEach(function (input) {
            input.disabled = listenMuted;
        });
    });

    connectStomp();
    fetchState();
    if (persistedMicEnabled) {
        window.setTimeout(function () {
            startVoice(true);
        }, 300);
    } else {
        startSignalRetryLoop();
    }

    window.addEventListener("beforeunload", function () {
        stopSignalRetryLoop();
        Array.from(peerMap.keys()).forEach(closePeer);
        Array.from(meterMap.keys()).forEach(stopVoiceMeter);
        if (localStream) {
            localStream.getTracks().forEach(function (track) { track.stop(); });
            localStream = null;
        }
        if (ws) ws.close();
    });

    function fetchState() {
        fetch("/comm/" + game + "/state")
            .then(function (response) { return response.json(); })
            .then(handleEvent)
            .catch(function () {
                els.policyText.textContent = "Không tải được trạng thái chat/voice.";
            });
    }

    function connectStomp() {
        const protocol = location.protocol === "https:" ? "wss://" : "ws://";
        ws = new WebSocket(protocol + location.host + "/ws", "v12.stomp");
        ws.onopen = function () {
            sendFrame("CONNECT", { "accept-version": "1.2", "heart-beat": "10000,10000" }, "");
        };
        ws.onclose = function () {
            stompConnected = false;
            setTimeout(connectStomp, 1500);
        };
        ws.onerror = function () {
            stompConnected = false;
        };
        ws.onmessage = function (event) {
            String(event.data || "").split("\u0000").forEach(function (frameText) {
                if (!frameText || !frameText.trim()) return;
                const frame = parseFrame(frameText);
                if (!frame) return;
                if (frame.command === "CONNECTED") {
                    stompConnected = true;
                    sendFrame("SUBSCRIBE", { id: "spy-comm-sub", destination: topic }, "");
                    sendFrame("SEND", { destination: "/app/comm.register", "content-type": "application/json" }, JSON.stringify({
                        game: game,
                        deviceId: deviceId,
                        playerName: myName
                    }));
                    requestSignalSync(true);
                    return;
                }
                if (frame.command === "MESSAGE") {
                    try {
                        handleEvent(JSON.parse(frame.body || "{}"));
                    } catch (_) {
                    }
                }
            });
        };
    }

    function sendFrame(command, headers, body) {
        if (!ws || ws.readyState !== WebSocket.OPEN) return;
        const lines = [command];
        Object.keys(headers || {}).forEach(function (key) {
            lines.push(key + ":" + headers[key]);
        });
        lines.push("");
        lines.push(body || "");
        ws.send(lines.join("\n") + "\u0000");
    }

    function parseFrame(raw) {
        const index = raw.indexOf("\n\n");
        const header = index >= 0 ? raw.substring(0, index) : raw;
        const body = index >= 0 ? raw.substring(index + 2) : "";
        const lines = header.split("\n");
        const command = (lines.shift() || "").trim();
        if (!command) return null;
        const headers = {};
        lines.forEach(function (line) {
            const at = line.indexOf(":");
            if (at > 0) headers[line.substring(0, at).trim()] = line.substring(at + 1).trim();
        });
        return { command: command, headers: headers, body: body };
    }

    function handleEvent(payload) {
        if (!payload || typeof payload !== "object") return;

        if (payload.type === "chat") {
            const channel = payload.channel || "public";
            if (!chatHistory[channel]) chatHistory[channel] = [];
            const line = (payload.senderName || "Ẩn danh") + ": " + (payload.message || "");
            chatHistory[channel].push(line);
            renderMessages();
            if (channel !== currentChannel || document.activeElement !== els.input) {
                showUnreadPopup();
            }
            return;
        }

        if (payload.type === "signal") {
            handleSignal(payload);
            return;
        }

        if (payload.type === "state" || payload.ok !== undefined) {
            state = payload;
            renderState();
            syncPeers();
            requestSignalSync(false);
        }
    }

    function renderState() {
        if (!state) return;

        const restrict = !!state.restrictEliminated;
        els.policyText.textContent = restrict
            ? "Chế độ tách người bị loại: BẬT. Người bị loại chỉ được chat/voice trong kênh riêng."
            : "Chế độ tách người bị loại: TẮT. Tất cả người chơi chat/voice chung.";

        const channels = Array.isArray(state.channels) ? state.channels : [];
        const previous = currentChannel;
        els.channelSelect.innerHTML = "";
        channels.forEach(function (channel) {
            const option = document.createElement("option");
            option.value = channel.key;
            option.textContent = channel.title + (channel.readOnly ? " (chỉ đọc)" : "");
            els.channelSelect.appendChild(option);
        });
        currentChannel = channels.some(function (c) { return c.key === previous; })
            ? previous
            : (channels[0] ? channels[0].key : "public");
        els.channelSelect.value = currentChannel;
        els.channelSelect.style.display = channels.length <= 1 ? "none" : "block";

        const participants = Array.isArray(state.participants) ? state.participants : [];
        els.participants.innerHTML = "";
        participants.forEach(function (participant) {
            const tag = document.createElement("span");
            tag.className = "badge " + (participant.alive ? "badge-green" : "badge-red");
            tag.textContent = (participant.userName || "Ẩn danh") + (participant.online ? "" : " (offline)");
            els.participants.appendChild(tag);
        });

        renderVolumeControls(participants);
        refreshComposerState();
        renderMessages();
    }

    function refreshComposerState() {
        const channels = Array.isArray(state && state.channels) ? state.channels : [];
        const selected = channels.find(function (item) { return item.key === currentChannel; });
        const canSend = !!(selected && selected.canSend);
        els.input.disabled = !canSend;
        els.sendBtn.disabled = !canSend;
        els.input.placeholder = canSend ? "Nhập tin nhắn..." : "Kênh hiện tại chỉ đọc";
    }

    function renderMessages() {
        const list = chatHistory[currentChannel] || [];
        els.messages.innerHTML = list.map(function (line) {
            return '<div style="margin-bottom:4px;word-break:break-word;">' + escapeHtml(line) + "</div>";
        }).join("");
        els.messages.scrollTop = els.messages.scrollHeight;
    }

    function showUnreadPopup() {
        unreadPopupVisible = true;
        els.popup.style.display = "inline-flex";
    }

    function hideUnreadPopup() {
        if (!unreadPopupVisible) return;
        unreadPopupVisible = false;
        els.popup.style.display = "none";
    }

    function sendChat() {
        if (!stompConnected || !state) return;
        const text = (els.input.value || "").trim();
        if (!text) return;
        sendFrame("SEND", { destination: "/app/comm.chat", "content-type": "application/json" }, JSON.stringify({
            game: game,
            deviceId: deviceId,
            channel: currentChannel,
            message: text,
            senderName: myName
        }));
        els.input.value = "";
    }

    function renderVolumeControls(participants) {
        els.volumeList.innerHTML = "";
        participants
            .filter(function (participant) { return participant.deviceId !== deviceId && participant.canHear; })
            .forEach(function (participant) {
                const targetDevice = participant.deviceId;
                const volume = getVolume(targetDevice);
                const row = document.createElement("div");
                row.style.display = "grid";
                row.style.gridTemplateColumns = "1fr 100px";
                row.style.gap = "8px";
                row.style.alignItems = "center";
                row.innerHTML =
                    '<div style="display:flex;align-items:center;justify-content:center;gap:8px;min-width:0;">' +
                    '<span data-speaking-light="' + targetDevice + '" style="width:8px;height:8px;border-radius:9999px;background:rgba(148,163,184,0.5);box-shadow:none;transition:all 120ms ease;"></span>' +
                    '<span style="font-size:0.78rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:140px;">' + escapeHtml(participant.userName || "Ẩn danh") + '</span>' +
                    '<div style="width:130px;max-width:45%;height:7px;border-radius:9999px;overflow:hidden;background:rgba(51,65,85,0.7);border:1px solid rgba(148,163,184,0.25);">' +
                    '<div data-speaking-meter="' + targetDevice + '" style="height:100%;width:0%;background:linear-gradient(90deg,#22c55e,#84cc16,#eab308,#f97316,#ef4444);transition:width 80ms linear;"></div>' +
                    '</div>' +
                    '</div>' +
                    '<input type="range" min="0" max="1.5" step="0.05" value="' + volume + '" data-volume-device="' + targetDevice + '">';
                els.volumeList.appendChild(row);
            });
        Array.from(els.volumeList.querySelectorAll("input[data-volume-device]")).forEach(function (input) {
            input.addEventListener("input", function () {
                const targetDevice = input.dataset.volumeDevice;
                const value = Number(input.value);
                volumePrefs[targetDevice] = value;
                saveVolumePrefs();
                applyRemoteVolume(targetDevice, value);
            });
            input.disabled = listenMuted;
        });
    }

    function syncPeers() {
        if (!state) return;
        const participants = Array.isArray(state.participants) ? state.participants : [];
        const allowed = new Map();
        participants.forEach(function (participant) {
            if (participant.deviceId === deviceId || !participant.online) return;
            if (participant.canHear || participant.canSend) {
                allowed.set(participant.deviceId, participant);
            }
        });

        Array.from(peerMap.keys()).forEach(function (targetDeviceId) {
            const existing = peerMap.get(targetDeviceId);
            const next = allowed.get(targetDeviceId);
            if (!next || existing.canSend !== !!next.canSend) {
                closePeer(targetDeviceId);
            }
        });

        allowed.forEach(function (participant, targetDeviceId) {
            if (!peerMap.has(targetDeviceId)) {
                createPeer(targetDeviceId, !!participant.canSend, deviceId < targetDeviceId);
            }
        });
    }

    function createPeer(targetDeviceId, canSendAudio, shouldOffer) {
        const peer = new RTCPeerConnection({ iceServers: [{ urls: "stun:stun.l.google.com:19302" }] });

        if (canSendAudio && localStream) {
            localStream.getAudioTracks().forEach(function (track) {
                peer.addTrack(track, localStream);
            });
        } else {
            peer.addTransceiver("audio", { direction: "recvonly" });
        }

        peer.onicecandidate = function (event) {
            if (!event.candidate) return;
            sendSignal(targetDeviceId, "ice", event.candidate);
        };

        peer.ontrack = function (event) {
            attachRemoteAudio(targetDeviceId, event.streams[0]);
        };

        peer.onconnectionstatechange = function () {
            if (["failed", "closed"].indexOf(peer.connectionState) >= 0) {
                closePeer(targetDeviceId);
            }
        };

        peerMap.set(targetDeviceId, { pc: peer, canSend: canSendAudio, createdAt: Date.now(), lastTrackAt: 0, restartCount: 0 });
        if (shouldOffer) createOfferFor(targetDeviceId);
    }

    function createOfferFor(targetDeviceId) {
        if (!stompConnected) return;
        const ref = peerMap.get(targetDeviceId);
        if (!ref || !ref.pc || ref.pc.signalingState !== "stable") return;
        ref.pc.createOffer()
            .then(function (offer) { return ref.pc.setLocalDescription(offer); })
            .then(function () {
                sendSignal(targetDeviceId, "offer", ref.pc.localDescription);
            })
            .catch(function () {
            });
    }

    function requestSignalSync(forceReady) {
        if (!stompConnected || !state) return;
        syncPeers();
        healPeers();
        const now = Date.now();
        if (forceReady || (now - lastReadySignalAt > 1200)) {
            lastReadySignalAt = now;
            sendReadySignals();
        }
    }

    function sendSignal(targetDeviceId, signalType, payload) {
        if (!stompConnected) return;
        sendFrame("SEND", { destination: "/app/comm.signal", "content-type": "application/json" }, JSON.stringify({
            game: game,
            deviceId: deviceId,
            targetDeviceId: targetDeviceId,
            signalType: signalType,
            payload: payload,
            fromInstanceId: clientInstanceId
        }));
    }

    function sendReadySignals() {
        if (!state || !stompConnected) return;
        const participants = Array.isArray(state.participants) ? state.participants : [];
        participants.forEach(function (participant) {
            if (participant.deviceId === deviceId || !participant.online || (!participant.canHear && !participant.canSend)) return;
            sendSignal(participant.deviceId, "ready", { instanceId: clientInstanceId, seq: ++readySeq });
        });
    }

    function handleSignal(payload) {
        if (!state) return;
        const fromDeviceId = payload.fromDeviceId;
        const signalType = payload.signalType;
        const signalPayload = payload.payload;
        const fromInstanceId = payload.fromInstanceId || "";
        const participant = findParticipant(fromDeviceId);
        if (!participant || !participant.online || (!participant.canHear && !participant.canSend)) return;

        const knownInstance = remoteInstanceByDevice.get(fromDeviceId);
        if (fromInstanceId && knownInstance && knownInstance !== fromInstanceId) {
            closePeer(fromDeviceId);
        }
        if (fromInstanceId) {
            remoteInstanceByDevice.set(fromDeviceId, fromInstanceId);
        }

        if (!peerMap.has(fromDeviceId)) {
            createPeer(fromDeviceId, !!participant.canSend, false);
        }
        const ref = peerMap.get(fromDeviceId);
        if (!ref || !ref.pc) return;
        const peer = ref.pc;

        if (signalType === "offer") {
            peer.setRemoteDescription(new RTCSessionDescription(signalPayload))
                .then(function () { return peer.createAnswer(); })
                .then(function (answer) { return peer.setLocalDescription(answer); })
                .then(function () {
                    sendSignal(fromDeviceId, "answer", peer.localDescription);
                })
                .catch(function () {
                });
            return;
        }

        if (signalType === "ready") {
            const readyInstance = signalPayload && signalPayload.instanceId ? String(signalPayload.instanceId) : "";
            const previousReadyInstance = knownInstance || "";
            const mustResetPeer = !!readyInstance && !!previousReadyInstance && readyInstance !== previousReadyInstance;
            if (mustResetPeer) {
                closePeer(fromDeviceId);
                createPeer(fromDeviceId, !!participant.canSend, false);
            }
            if (deviceId < fromDeviceId) {
                createOfferFor(fromDeviceId);
            }
            return;
        }

        if (signalType === "answer") {
            peer.setRemoteDescription(new RTCSessionDescription(signalPayload)).catch(function () {
            });
            return;
        }

        if (signalType === "ice") {
            peer.addIceCandidate(new RTCIceCandidate(signalPayload)).catch(function () {
            });
        }
    }

    function closePeer(targetDeviceId) {
        const ref = peerMap.get(targetDeviceId);
        if (!ref) return;
        try {
            ref.pc.close();
        } catch (_) {
        }
        peerMap.delete(targetDeviceId);

        const audio = audioMap.get(targetDeviceId);
        if (audio) {
            audio.srcObject = null;
            audio.remove();
            audioMap.delete(targetDeviceId);
        }
        stopVoiceMeter(targetDeviceId);
        updateSpeakingVisual(targetDeviceId, 0);
        remoteInstanceByDevice.delete(targetDeviceId);
    }

    function attachRemoteAudio(targetDeviceId, stream) {
        let audio = audioMap.get(targetDeviceId);
        if (!audio) {
            audio = document.createElement("audio");
            audio.autoplay = true;
            audio.playsInline = true;
            audio.style.display = "none";
            document.body.appendChild(audio);
            audioMap.set(targetDeviceId, audio);
        }
        audio.srcObject = stream;
        audio.muted = listenMuted;
        if (typeof audio.play === "function") {
            audio.play().catch(function () {
            });
        }
        const ref = peerMap.get(targetDeviceId);
        if (ref) {
            ref.lastTrackAt = Date.now();
        }
        applyRemoteVolume(targetDeviceId, getVolume(targetDeviceId));
        startVoiceMeter(targetDeviceId, stream);
    }

    function applyRemoteVolume(targetDeviceId, value) {
        const audio = audioMap.get(targetDeviceId);
        if (audio) {
            audio.muted = listenMuted;
            audio.volume = Math.max(0, Math.min(1.5, Number(value)));
        }
    }

    function getVolume(targetDeviceId) {
        const value = volumePrefs[targetDeviceId];
        return Number.isFinite(value) ? value : 1;
    }

    function loadVolumePrefs() {
        try {
            return JSON.parse(localStorage.getItem(volumeStorageKey) || "{}");
        } catch (_) {
            return {};
        }
    }

    function saveVolumePrefs() {
        localStorage.setItem(volumeStorageKey, JSON.stringify(volumePrefs));
    }

    function loadMicEnabledPreference() {
        return localStorage.getItem(micEnabledStorageKey) === "1";
    }

    function saveMicEnabledPreference(enabled) {
        localStorage.setItem(micEnabledStorageKey, enabled ? "1" : "0");
    }

    function loadListenMutedPreference() {
        return localStorage.getItem(listenMutedStorageKey) === "1";
    }

    function saveListenMutedPreference(enabled) {
        localStorage.setItem(listenMutedStorageKey, enabled ? "1" : "0");
    }

    async function startVoice(autoMode) {
        if (micEnabled || voiceStarting) {
            return;
        }
        voiceStarting = true;
        micEnabled = true;
        saveMicEnabledPreference(true);
        updateMicButton();
        try {
            localStream = await requestMicStream();
            if (localStream) {
                voiceStatusBase = "Voice nghe đang bật. Mic đang bật.";
            } else {
                micEnabled = false;
                saveMicEnabledPreference(false);
                updateMicButton();
                voiceStatusBase = "Voice nghe đang bật. Máy không hỗ trợ micro.";
            }
        } catch (error) {
            localStream = null;
            micEnabled = false;
            saveMicEnabledPreference(false);
            updateMicButton();
            if (autoMode) {
                voiceStatusBase = "Voice nghe tự khôi phục. Mic chưa sẵn sàng.";
            } else {
                voiceStatusBase = "Voice nghe đang bật. Micro lỗi: " + (error && error.message ? error.message : "Lỗi");
            }
        } finally {
            voiceStarting = false;
        }
        renderVoiceStatus();
        rebuildPeersForMicChange();
        startSignalRetryLoop();
    }

    function stopVoice(clearPreference) {
        micEnabled = false;
        if (clearPreference !== false) {
            saveMicEnabledPreference(false);
        }
        updateMicButton();
        voiceStatusBase = "Voice nghe đang bật. Mic đang tắt.";
        renderVoiceStatus();
        if (localStream) {
            localStream.getTracks().forEach(function (track) { track.stop(); });
            localStream = null;
        }
        rebuildPeersForMicChange();
    }

    async function requestMicStream() {
        if (navigator.mediaDevices && typeof navigator.mediaDevices.getUserMedia === "function") {
            return navigator.mediaDevices.getUserMedia({ audio: true, video: false });
        }
        const legacyGetUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
        if (typeof legacyGetUserMedia === "function") {
            return new Promise(function (resolve, reject) {
                legacyGetUserMedia.call(navigator, { audio: true, video: false }, resolve, reject);
            });
        }
        return null;
    }

    function startSignalRetryLoop() {
        stopSignalRetryLoop();
        signalRetryTimer = window.setInterval(function () {
            requestSignalSync(false);
        }, 2500);
    }

    function stopSignalRetryLoop() {
        if (!signalRetryTimer) return;
        window.clearInterval(signalRetryTimer);
        signalRetryTimer = 0;
    }

    function rebuildPeersForMicChange() {
        Array.from(peerMap.keys()).forEach(closePeer);
        syncPeers();
        requestSignalSync(true);
    }

    function updateMicButton() {
        if (micEnabled) {
            els.voiceBtn.textContent = "Tắt Mic";
            els.voiceBtn.classList.remove("btn-danger");
            els.voiceBtn.classList.add("btn-success");
        } else {
            els.voiceBtn.textContent = "Bật Mic";
            els.voiceBtn.classList.remove("btn-success");
            els.voiceBtn.classList.add("btn-danger");
        }
    }

    function updateListenButton() {
        if (listenMuted) {
            els.listenBtn.textContent = "Bật âm";
            els.listenBtn.classList.remove("btn-primary");
            els.listenBtn.classList.add("btn-danger");
        } else {
            els.listenBtn.textContent = "Tắt âm";
            els.listenBtn.classList.remove("btn-danger");
            els.listenBtn.classList.add("btn-primary");
        }
    }

    function renderVoiceStatus() {
        const listenText = listenMuted ? "Âm loa đang tắt." : "Âm loa đang bật.";
        els.voiceStatus.textContent = voiceStatusBase + " " + listenText;
    }

    function applyListenMuteToAll() {
        Array.from(audioMap.values()).forEach(function (audio) {
            if (!audio) return;
            audio.muted = listenMuted;
        });
    }

    function healPeers() {
        const now = Date.now();
        Array.from(peerMap.entries()).forEach(function (entry) {
            const targetDeviceId = entry[0];
            const ref = entry[1];
            if (!ref || !ref.pc) return;

            const peer = ref.pc;
            const hasAudio = audioMap.has(targetDeviceId);
            const age = now - (ref.createdAt || now);
            const noTrackForLong = !hasAudio && age > 7000;
            const failedState = ["failed", "closed"].indexOf(peer.connectionState) >= 0;
            const iceFailed = peer.iceConnectionState === "failed";

            if (failedState || iceFailed) {
                const canSendAudio = !!ref.canSend;
                const nextRestart = (ref.restartCount || 0) + 1;
                closePeer(targetDeviceId);
                createPeer(targetDeviceId, canSendAudio, deviceId < targetDeviceId);
                const created = peerMap.get(targetDeviceId);
                if (created) {
                    created.restartCount = nextRestart;
                }
                return;
            }

            if (noTrackForLong && deviceId < targetDeviceId && peer.signalingState === "stable") {
                createOfferFor(targetDeviceId);
                ref.createdAt = now;
            }
        });
    }

    function startVoiceMeter(targetDeviceId, stream) {
        stopVoiceMeter(targetDeviceId);
        if (!stream || !(window.AudioContext || window.webkitAudioContext)) return;
        const Ctx = window.AudioContext || window.webkitAudioContext;
        const context = new Ctx();
        const analyser = context.createAnalyser();
        analyser.fftSize = 256;
        analyser.smoothingTimeConstant = 0.72;
        const source = context.createMediaStreamSource(stream);
        source.connect(analyser);
        const data = new Uint8Array(analyser.fftSize);
        const meter = { context: context, analyser: analyser, source: source, data: data, rafId: 0 };
        meterMap.set(targetDeviceId, meter);

        const loop = function () {
            const current = meterMap.get(targetDeviceId);
            if (!current) return;
            current.analyser.getByteTimeDomainData(current.data);
            let sum = 0;
            for (let i = 0; i < current.data.length; i++) {
                const value = (current.data[i] - 128) / 128;
                sum += value * value;
            }
            const rms = Math.sqrt(sum / current.data.length);
            const normalized = Math.max(0, Math.min(1, rms * 3.2));
            updateSpeakingVisual(targetDeviceId, normalized);
            current.rafId = requestAnimationFrame(loop);
        };
        meter.rafId = requestAnimationFrame(loop);
    }

    function stopVoiceMeter(targetDeviceId) {
        const meter = meterMap.get(targetDeviceId);
        if (!meter) return;
        if (meter.rafId) cancelAnimationFrame(meter.rafId);
        try {
            meter.source.disconnect();
        } catch (_) {
        }
        try {
            meter.analyser.disconnect();
        } catch (_) {
        }
        try {
            meter.context.close();
        } catch (_) {
        }
        meterMap.delete(targetDeviceId);
    }

    function updateSpeakingVisual(targetDeviceId, level) {
        const meter = document.querySelector('[data-speaking-meter="' + targetDeviceId + '"]');
        if (meter) {
            meter.style.width = Math.round(level * 100) + "%";
        }
        const light = document.querySelector('[data-speaking-light="' + targetDeviceId + '"]');
        if (light) {
            if (level > 0.08) {
                light.style.background = "rgba(34,197,94,0.95)";
                light.style.boxShadow = "0 0 10px rgba(34,197,94,0.95)";
            } else {
                light.style.background = "rgba(148,163,184,0.5)";
                light.style.boxShadow = "none";
            }
        }
    }

    function findParticipant(targetDeviceId) {
        const participants = Array.isArray(state && state.participants) ? state.participants : [];
        return participants.find(function (item) { return item.deviceId === targetDeviceId; }) || null;
    }

    function escapeHtml(text) {
        return String(text || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }
})();
