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
    const micDeviceStorageKey = "spy-comm-mic-device-" + game + "-" + deviceId;
    const speakerDeviceStorageKey = "spy-comm-speaker-device-" + game + "-" + deviceId;
    const chatHistoryStorageKey = "spy-comm-chat-history-" + game + "-" + deviceId;
    const viewModeStorageKey = "spy-comm-view-mode-" + game + "-" + deviceId;
    const maxChatPerChannel = 250;
    const chatEmojis = [
                           "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
                           "🫠", "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "☺️",
                           "😚", "😙", "🥲", "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗",
                           "🤭", "🫢", "🫣", "🤫", "🤔", "🫡", "🤐", "🤨", "😐", "😑",
                           "😶", "🫥", "😶‍🌫️", "😏", "😒", "🙄", "😬", "😮‍💨", "🤥", "🫨",
                           "🙂‍↔️", "🙂‍↕️", "😌", "😔", "😪", "🤤", "😴", "🫩", "😷", "🤒",
                           "🤕", "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "😵‍💫", "🤯",
                           "🤠", "🥳", "🥸", "😎", "🤓", "🧐", "😕", "🫤", "😟", "🙁",
                           "☹️", "😮", "😯", "😲", "😳", "🥺", "🥹", "😦", "😧", "😨",
                           "😰", "😥", "😢", "😭", "😱", "😖", "😣", "😞", "😓", "😩",
                           "😫", "🥱", "😤", "😡", "😠", "🤬", "😈", "👿", "💀", "☠️",
                           "💩", "🤡", "👹", "👺", "👻", "👽", "👾", "🤖", "🎃",
                           "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾",
                           "🙈", "🙉", "🙊",
                            "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "🫳", "🫴",
                               "🫷", "🫸", "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟",
                               "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "🫵",
                               "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "🫶",
                               "👐", "🤲", "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾",
                               "🦿", "🦵", "🦶", "👂", "🦻", "👃", "👀", "👁️", "🫦",
                               "👄", "🫶", "💋", "🧠", "🫀", "🫁", "🦷", "🦴",
                               "🐑", "🐐", "🐪", "🐫", "🦙", "🦒", "🐘", "🦣", "🦏",
                                   "🦛", "🐭", "🐁", "🐀", "🐹", "🐰", "🐇", "🐿️", "🦫",
                                   "🦔", "🦇", "🐻", "🐻‍❄️", "🐨", "🐼", "🦥", "🦦", "🦨",
                                   "🦘", "🦡", "🐾", "🦃", "🐔", "🐓", "🐣", "🐤", "🐥",
                                   "🐦", "🐧", "🕊️", "🦅", "🦆", "🦢", "🦉", "🦤", "🪶",
                                   "🦩", "🦚", "🦜", "🪽", "🐦‍⬛", "🪿", "🐸", "🐊", "🐢",
                                   "🦎", "🐍", "🐲", "🐉", "🦕", "🦖", "🐳", "🐋", "🐬",
                                   "🦭", "🐟", "🐠", "🐡", "🦈", "🐙", "🐚", "🪸", "🪼",
                                   "🦀", "🦞", "🦐", "🦑", "🦪", "🐌", "🦋", "🐛", "🐜",
                                   "🐝", "🪲", "🐞", "🦗", "🪳", "🕷️", "🕸️", "🦂", "🦟",
                                   "🪰", "🪱", "🦠", "💐", "🌸", "💮", "🪷", "🏵️", "🌹",
                                   "🥀", "🌺", "🌻", "🌼", "🌷", "🪻", "🌱", "🪴", "🌲",
                                   "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁", "🍂",
                                   "🍃", "🪹", "🪺", "🍄", "🌰", "🪵", "🌍", "🌎", "🌏",
                                   "🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘", "🌙",
                                   "🌚", "🌛", "🌜", "☀️", "🌝", "🌞", "🪐", "⭐", "🌟",
                                   "🌠", "🌌", "☁️", "⛅", "⛈️", "🌤️", "🌥️", "🌦️",
                                   "🌧️", "🌨️", "🌩️", "🌪️", "🌫️", "🌬️", "🌀", "🌈",
                                   "🌂", "☂️", "☔", "⛱️", "⚡", "❄️", "☃️", "⛄",
                                   "☄️", "🔥", "💧", "🌊"
                       ];
    const effectTokenPrefix = "__fx__:";
    const screenEffects = [
        { key: "punch", icon: "👊", label: "👊👊" },
        { key: "punchCombo", icon: "🥊", label: "🥊🥊" },
        { key: "heart", icon: "❤️", label: "❤️❤️" },
        { key: "ant", icon: "🐜", label: "🐜🐜" },
        { key: "fly", icon: "🪰", label: "🪰🪰" },
        { key: "lightning", icon: "⚡", label: "⚡⚡" },
        { key: "elephant", icon: "🐘", label: "🐘🐘" },
        { key: "fall", icon: "🫠", label: "🫠🫠" }
    ];

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
    let selectedMicDeviceId = loadMicDevicePreference();
    let selectedSpeakerDeviceId = loadSpeakerDevicePreference();
    let viewMode = loadViewModePreference();
    let compactChatOpen = false;
    let settingPanelOpen = false;
    let screenShakeTimer = 0;
    let fallRestoreTimer = 0;
    const unreadChannels = {};
    let voiceStatusBase = "Voice nghe đang bật. Mic đang tắt.";
    const remoteInstanceByDevice = new Map();

    const peerMap = new Map();
    const audioMap = new Map();
    const meterMap = new Map();
    const canSetSinkId = !!(window.HTMLMediaElement && window.HTMLMediaElement.prototype && typeof window.HTMLMediaElement.prototype.setSinkId === "function");
    const chatHistory = loadChatHistory();
    const volumePrefs = loadVolumePrefs();
    const persistedMicEnabled = loadMicEnabledPreference();
    listenMuted = loadListenMutedPreference();

    root.innerHTML = [
        "<style>",
        ".comm-float-dock{position:fixed;top:12px;right:12px;display:flex;flex-direction:column;gap:8px;z-index:1200;}",
        ".comm-float-btn{width:44px;height:44px;border-radius:12px;border:1px solid rgba(255,255,255,0.25);background:rgba(15,23,42,0.92);color:#e2e8f0;font-size:19px;display:inline-flex;align-items:center;justify-content:center;cursor:pointer;box-shadow:0 8px 20px rgba(0,0,0,0.35);padding:0;}",
        ".comm-float-btn.active{border-color:rgba(96,165,250,0.85);background:rgba(30,41,59,0.98);}",
        ".comm-float-btn.state-on{background:rgba(22,163,74,0.92);border-color:rgba(74,222,128,0.9);color:#ecfdf5;}",
        ".comm-float-btn.state-off{background:rgba(220,38,38,0.92);border-color:rgba(252,165,165,0.9);color:#fff1f2;}",
        ".comm-float-btn.has-unread{background:rgba(217,119,6,0.95);border-color:rgba(253,186,116,0.95);color:#fff7ed;animation:commPulse 1.2s ease-in-out infinite;}",
        ".comm-float-btn.has-unread::after{content:'';position:absolute;top:5px;right:5px;width:10px;height:10px;border-radius:999px;background:#fef08a;box-shadow:0 0 0 2px rgba(15,23,42,0.95),0 0 10px rgba(254,240,138,0.9);}",
        ".comm-channel-tabs{display:flex;gap:6px;flex-wrap:wrap;}",
        ".comm-channel-tab{position:relative;border:1px solid var(--card-border);background:rgba(30,41,59,0.75);color:#cbd5e1;padding:6px 10px;border-radius:10px;font-size:0.78rem;font-weight:700;cursor:pointer;}",
        ".comm-channel-tab.active{background:rgba(37,99,235,0.85);border-color:rgba(96,165,250,0.9);color:#eff6ff;}",
        ".comm-channel-tab.has-unread{background:rgba(217,119,6,0.92);border-color:rgba(253,186,116,0.95);color:#fffbeb;animation:commPulse 1.2s ease-in-out infinite;}",
        ".comm-channel-tab.has-unread::after{content:'';position:absolute;top:4px;right:4px;width:8px;height:8px;border-radius:999px;background:#fef08a;box-shadow:0 0 0 2px rgba(15,23,42,0.95),0 0 8px rgba(254,240,138,0.8);}",
        "@keyframes commPulse{0%,100%{transform:scale(1);}50%{transform:scale(1.08);}}",
        ".comm-mini-panel{position:fixed;top:12px;right:64px;width:min(340px,calc(100vw - 90px));border:1px solid var(--card-border);border-radius:12px;background:rgba(15,23,42,0.97);padding:10px;z-index:1190;box-shadow:0 18px 36px rgba(0,0,0,0.45);display:none;}",
        ".comm-mini-title{font-size:0.82rem;font-weight:700;color:#93c5fd;margin-bottom:8px;}",
        ".comm-setting-row{display:flex;align-items:center;justify-content:space-between;gap:10px;font-size:0.83rem;color:#e2e8f0;}",
        ".comm-switch{position:relative;display:inline-block;width:46px;height:26px;}",
        ".comm-switch input{opacity:0;width:0;height:0;}",
        ".comm-slider{position:absolute;cursor:pointer;top:0;left:0;right:0;bottom:0;background:rgba(30,41,59,0.9);border:1px solid var(--card-border);transition:.2s;border-radius:999px;}",
        ".comm-slider:before{content:'';position:absolute;height:18px;width:18px;left:3px;top:3px;background:#94a3b8;transition:.2s;border-radius:50%;}",
        ".comm-switch input:checked + .comm-slider{background:rgba(59,130,246,0.85);}",
        ".comm-switch input:checked + .comm-slider:before{transform:translateX(20px);background:#fff;}",
        ".comm-emoji-wrap{position:relative;display:flex;align-items:center;}",
        ".comm-emoji-btn{width:38px;height:38px;border-radius:10px;border:1px solid var(--card-border);background:rgba(30,41,59,0.85);color:#fde68a;font-size:19px;cursor:pointer;}",
        ".comm-emoji-btn:disabled{opacity:.55;cursor:not-allowed;}",
        ".comm-emoji-picker{position:absolute;right:0;bottom:44px;display:none;grid-template-columns:repeat(6,minmax(0,1fr));gap:6px;padding:8px;border-radius:10px;border:1px solid var(--card-border);background:rgba(15,23,42,0.98);width:196px;max-height:168px;overflow-y:auto;overflow-x:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.45);z-index:1300;scrollbar-width:thin;scrollbar-color:rgba(148,163,184,0.7) rgba(15,23,42,0.65);}",
        ".comm-emoji-picker.open{display:grid;}",
        ".comm-emoji-picker::-webkit-scrollbar{width:8px;}",
        ".comm-emoji-picker::-webkit-scrollbar-track{background:rgba(15,23,42,0.65);border-radius:999px;}",
        ".comm-emoji-picker::-webkit-scrollbar-thumb{background:rgba(148,163,184,0.7);border-radius:999px;}",
        ".comm-emoji-item{border:none;border-radius:8px;background:rgba(30,41,59,0.9);font-size:18px;line-height:1;padding:4px 0;cursor:pointer;}",
        ".comm-emoji-item:hover{background:rgba(59,130,246,0.45);}",
        ".comm-fx-wrap{position:relative;display:flex;align-items:center;}",
        ".comm-fx-btn{width:38px;height:38px;border-radius:10px;border:1px solid rgba(248,113,113,0.55);background:rgba(127,29,29,0.75);color:#fecaca;font-size:18px;cursor:pointer;}",
        ".comm-fx-btn:disabled{opacity:.55;cursor:not-allowed;}",
        ".comm-fx-picker{position:absolute;right:0;bottom:44px;display:none;grid-template-columns:1fr;gap:6px;padding:8px;border-radius:10px;border:1px solid rgba(248,113,113,0.35);background:rgba(15,23,42,0.98);width:150px;max-height:200px;overflow-y:auto;overflow-x:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.45);z-index:1300;}",
        ".comm-fx-picker.open{display:grid;}",
        ".comm-fx-item{display:flex;align-items:center;gap:8px;border:none;border-radius:8px;background:rgba(127,29,29,0.35);color:#fee2e2;font-size:0.82rem;padding:8px 10px;cursor:pointer;text-align:left;}",
        ".comm-fx-item:hover{background:rgba(239,68,68,0.45);}",
        ".comm-fx-layer{position:fixed;inset:0;pointer-events:none;z-index:2500;overflow:hidden;}",
        ".comm-screen-shake{animation:commScreenShake 0.45s ease;}",
        ".comm-screen-shake-hard{animation:commScreenShakeHard 1.9s linear;}",
        "@keyframes commScreenShake{0%{transform:translate(0,0);}15%{transform:translate(-8px,6px);}30%{transform:translate(8px,-6px);}45%{transform:translate(-6px,4px);}60%{transform:translate(6px,-4px);}75%{transform:translate(-4px,2px);}100%{transform:translate(0,0);}}",
        "@keyframes commScreenShakeHard{0%{transform:translate(0,0);}10%{transform:translate(-12px,7px);}20%{transform:translate(11px,-8px);}30%{transform:translate(-10px,6px);}40%{transform:translate(9px,-7px);}50%{transform:translate(-8px,7px);}60%{transform:translate(8px,-6px);}70%{transform:translate(-7px,5px);}80%{transform:translate(6px,-4px);}90%{transform:translate(-4px,3px);}100%{transform:translate(0,0);}}",
        ".comm-fx-punch{position:absolute;left:50%;top:50%;font-size:18vw;transform:translate(-50%,-50%) scale(0.2);animation:commFxPunch 0.62s ease-out forwards;filter:drop-shadow(0 0 24px rgba(248,113,113,0.7));}",
        "@keyframes commFxPunch{0%{opacity:0;transform:translate(-50%,-50%) scale(0.2);}20%{opacity:1;}70%{opacity:1;transform:translate(-50%,-50%) scale(1.6);}100%{opacity:0;transform:translate(-50%,-50%) scale(3.2);}}",
        ".comm-fx-heart{position:absolute;bottom:-40px;font-size:28px;animation:commFxHeart var(--dur,1.9s) ease-in forwards;}",
        "@keyframes commFxHeart{0%{opacity:0;transform:translate(0,0) scale(0.8);}12%{opacity:1;}100%{opacity:0;transform:translate(var(--drift,0px),-120vh) scale(1.6);}}",
        ".comm-fx-bug{position:absolute;font-size:30px;filter:drop-shadow(0 0 8px rgba(0,0,0,0.35));will-change:transform,left,top;}",
        ".comm-fx-flash{position:absolute;inset:0;background:radial-gradient(circle at 50% 22%,rgba(255,255,220,0.82),rgba(251,191,36,0.42) 38%,rgba(15,23,42,0.12) 72%);animation:commFxFlash var(--dur,0.32s) ease-out forwards;}",
        "@keyframes commFxFlash{0%{opacity:0;}20%{opacity:1;}100%{opacity:0;}}",
        ".comm-fx-bolt{position:absolute;top:-18vh;left:50%;font-size:20vw;transform:translateX(-50%);animation:commFxBolt 0.58s ease-out forwards;filter:drop-shadow(0 0 22px rgba(250,204,21,0.95));}",
        "@keyframes commFxBolt{0%{opacity:0;transform:translateX(-50%) translateY(-30vh) scale(0.7);}15%{opacity:1;}55%{opacity:1;transform:translateX(-50%) translateY(40vh) scale(1);}100%{opacity:0;transform:translateX(-50%) translateY(70vh) scale(1.15);}}",
        ".comm-fx-lightning-svg{position:absolute;inset:0;overflow:visible;filter:drop-shadow(0 0 10px rgba(253,224,71,0.95)) drop-shadow(0 0 22px rgba(250,204,21,0.72));animation:commFxLightning .45s steps(1,end) forwards;}",
        "@keyframes commFxLightning{0%{opacity:0;}8%{opacity:1;}18%{opacity:.25;}24%{opacity:1;}38%{opacity:.45;}50%{opacity:1;}100%{opacity:0;}}",
        ".comm-fx-impact{position:absolute;left:0;top:0;width:92px;height:92px;transform:translate(-50%,-50%);pointer-events:none;border-radius:999px;background:radial-gradient(circle,rgba(255,255,255,0.95) 0%,rgba(250,204,21,0.72) 34%,rgba(250,204,21,0) 70%);animation:commFxImpact .52s ease-out forwards;}",
        "@keyframes commFxImpact{0%{opacity:0;transform:translate(-50%,-50%) scale(0.16);}25%{opacity:1;}100%{opacity:0;transform:translate(-50%,-50%) scale(1.35);}}",
        ".comm-fx-elephant{position:absolute;left:124vw;font-size:52px;animation:commFxElephant var(--dur,7.2s) linear forwards;animation-delay:var(--delay,0s);}",
        "@keyframes commFxElephant{0%{transform:translateX(0);}100%{transform:translateX(-170vw);}}",
        "body.comm-fall-mode .comm-fall-piece{transform-origin:center top;animation:commFxFallPiece 4.2s ease-in-out both !important;animation-delay:var(--fall-delay,0s) !important;}",
        "@keyframes commFxFallPiece{0%{transform:translate(0,0) rotate(0deg);opacity:1;}18%{transform:translate(0,0) rotate(0deg);opacity:1;}52%{transform:translate(var(--fall-drift,0px),var(--fall-drop,42vh)) rotate(var(--fall-rot,8deg));opacity:0.16;}70%{transform:translate(var(--fall-drift,0px),var(--fall-drop,42vh)) rotate(var(--fall-rot,8deg));opacity:0.08;}100%{transform:translate(0,0) rotate(0deg);opacity:1;}}",
        "</style>",
        '<div id="commExpandedWrap" class="card" style="display:flex;flex-direction:column;gap:10px;">',
        '<h3 style="margin:0 0 4px 0;text-align:center;">Chat & Voice</h3>',
        '<div id="commDeviceRow" style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:8px;">',
        '<div style="display:flex;flex-direction:column;gap:4px;">',
        '<span style="font-size:0.75rem;color:var(--text-muted);">Mic đầu vào</span>',
        '<select id="commMicSelect" class="input-text" style="padding:7px 8px;"></select>',
        "</div>",
        '<div style="display:flex;flex-direction:column;gap:4px;">',
        '<span style="font-size:0.75rem;color:var(--text-muted);">Loa đầu ra</span>',
        '<select id="commSpeakerSelect" class="input-text" style="padding:7px 8px;"></select>',
        "</div>",
        "</div>",
        '<div id="commPolicyText" style="font-size:0.82rem;color:var(--text-muted);text-align:center;"></div>',
        '<div id="commParticipants" style="font-size:0.8rem;display:flex;flex-wrap:wrap;gap:6px;"></div>',
        '<div id="commControlRow" style="display:flex;gap:8px;align-items:center;">',
        '<button id="commVoiceBtn" class="btn btn-primary" style="width:auto;padding:8px 14px;">Bật Voice</button>',
        '<button id="commListenBtn" class="btn btn-primary" style="width:auto;padding:8px 14px;">Tắt âm</button>',
        '</div>',
        '<div id="commVoiceStatus" style="font-size:0.78rem;color:var(--text-muted);"></div>',
        '<div id="commVolumeList" style="display:flex;flex-direction:column;gap:6px;"></div>',
        '<div id="commChannelTabs" class="comm-channel-tabs" style="display:none;margin-top:8px;margin-bottom:8px;"></div>',
        '<div id="commMessages" style="height:170px;overflow:auto;border:1px solid var(--card-border);border-radius:10px;padding:8px;background:rgba(15,23,42,0.45);font-size:0.82rem;text-align:left;"></div>',
        '<div id="commComposerRow" style="display:flex;gap:8px;">',
        '<input id="commInput" class="input-text" maxlength="220" placeholder="Nhập tin nhắn..." style="flex:1;" />',
        '<div id="commEmojiWrap" class="comm-emoji-wrap">',
        '<button id="commEmojiBtn" type="button" class="comm-emoji-btn" title="Chọn emoji">😊</button>',
        '<div id="commEmojiPicker" class="comm-emoji-picker"></div>',
        "</div>",
        '<div id="commFxWrap" class="comm-fx-wrap">',
        '<button id="commFxBtn" type="button" class="comm-fx-btn" title="Screen Shake Effect">💥</button>',
        '<div id="commFxPicker" class="comm-fx-picker"></div>',
        "</div>",
        '<button id="commSendBtn" class="btn btn-primary" style="width:auto;padding:8px 14px;">Gửi</button>',
        "</div>",
        "</div>",
        '<div id="commDock" class="comm-float-dock">',
        '<button id="commDockSettingBtn" type="button" class="comm-float-btn" title="Cài đặt">⚙️</button>',
        '<button id="commDockMicBtn" type="button" class="comm-float-btn" title="Bật/tắt mic">🎤</button>',
        '<button id="commDockListenBtn" type="button" class="comm-float-btn" title="Bật/tắt âm">🔊</button>',
        '<button id="commDockChatBtn" type="button" class="comm-float-btn" title="Hiển thị chat">💬</button>',
        "</div>",
        '<div id="commMiniChatPanel" class="comm-mini-panel">',
        '<div class="comm-mini-title">Chat nhanh</div>',
        '<div id="commMiniChannelTabs" class="comm-channel-tabs" style="display:none;margin-bottom:8px;"></div>',
        '<div id="commMiniMessages" style="height:160px;overflow:auto;border:1px solid var(--card-border);border-radius:10px;padding:8px;background:rgba(15,23,42,0.45);font-size:0.82rem;text-align:left;"></div>',
        '<div style="display:flex;gap:8px;margin-top:8px;">',
        '<input id="commMiniInput" class="input-text" maxlength="220" placeholder="Nhập tin nhắn..." style="flex:1;" />',
        '<div id="commMiniEmojiWrap" class="comm-emoji-wrap">',
        '<button id="commMiniEmojiBtn" type="button" class="comm-emoji-btn" title="Chọn emoji">😊</button>',
        '<div id="commMiniEmojiPicker" class="comm-emoji-picker"></div>',
        "</div>",
        '<div id="commMiniFxWrap" class="comm-fx-wrap">',
        '<button id="commMiniFxBtn" type="button" class="comm-fx-btn" title="Screen Shake Effect">💥</button>',
        '<div id="commMiniFxPicker" class="comm-fx-picker"></div>',
        "</div>",
        '<button id="commMiniSendBtn" class="btn btn-primary" style="width:auto;padding:8px 12px;">Gửi</button>',
        "</div>",
        "</div>",
        '<div id="commSettingPanel" class="comm-mini-panel">',
        '<div class="comm-mini-title">Cài đặt hiển thị</div>',
        '<div class="comm-setting-row">',
        '<span id="commViewModeLabel">Đang phóng to</span>',
        '<label class="comm-switch">',
        '<input id="commViewModeToggle" type="checkbox" checked>',
        '<span class="comm-slider"></span>',
        "</label>",
        "</div>",
        "</div>",
        '<div id="commFxLayer" class="comm-fx-layer"></div>',
        '<button id="commUnreadPopup" type="button" style="position:fixed;right:16px;bottom:18px;z-index:1000;display:none;padding:10px 14px;border-radius:12px;border:1px solid rgba(96,165,250,0.45);background:rgba(15,23,42,0.95);color:#93c5fd;font-weight:700;font-size:0.82rem;box-shadow:0 4px 18px rgba(0,0,0,0.35);">💬 Có tin nhắn mới</button>'
    ].join("");

    const els = {
        expandedWrap: document.getElementById("commExpandedWrap"),
        policyText: document.getElementById("commPolicyText"),
        participants: document.getElementById("commParticipants"),
        channelTabs: document.getElementById("commChannelTabs"),
        miniChannelTabs: document.getElementById("commMiniChannelTabs"),
        controlRow: document.getElementById("commControlRow"),
        voiceBtn: document.getElementById("commVoiceBtn"),
        listenBtn: document.getElementById("commListenBtn"),
        voiceStatus: document.getElementById("commVoiceStatus"),
        micSelect: document.getElementById("commMicSelect"),
        speakerSelect: document.getElementById("commSpeakerSelect"),
        volumeList: document.getElementById("commVolumeList"),
        messages: document.getElementById("commMessages"),
        input: document.getElementById("commInput"),
        sendBtn: document.getElementById("commSendBtn"),
        dock: document.getElementById("commDock"),
        dockSettingBtn: document.getElementById("commDockSettingBtn"),
        dockMicBtn: document.getElementById("commDockMicBtn"),
        dockListenBtn: document.getElementById("commDockListenBtn"),
        dockChatBtn: document.getElementById("commDockChatBtn"),
        miniPanel: document.getElementById("commMiniChatPanel"),
        miniMessages: document.getElementById("commMiniMessages"),
        miniInput: document.getElementById("commMiniInput"),
        miniSendBtn: document.getElementById("commMiniSendBtn"),
        emojiWrap: document.getElementById("commEmojiWrap"),
        emojiBtn: document.getElementById("commEmojiBtn"),
        emojiPicker: document.getElementById("commEmojiPicker"),
        fxWrap: document.getElementById("commFxWrap"),
        fxBtn: document.getElementById("commFxBtn"),
        fxPicker: document.getElementById("commFxPicker"),
        miniEmojiWrap: document.getElementById("commMiniEmojiWrap"),
        miniEmojiBtn: document.getElementById("commMiniEmojiBtn"),
        miniEmojiPicker: document.getElementById("commMiniEmojiPicker"),
        miniFxWrap: document.getElementById("commMiniFxWrap"),
        miniFxBtn: document.getElementById("commMiniFxBtn"),
        miniFxPicker: document.getElementById("commMiniFxPicker"),
        settingPanel: document.getElementById("commSettingPanel"),
        viewModeToggle: document.getElementById("commViewModeToggle"),
        viewModeLabel: document.getElementById("commViewModeLabel"),
        fxLayer: document.getElementById("commFxLayer"),
        popup: document.getElementById("commUnreadPopup")
    };
    initEmojiPickers();
    initFxPickers();
    updateMicButton();
    updateListenButton();
    renderVoiceStatus();
    applyViewMode();
    renderMessages();
    refreshAudioDeviceOptions();
    if (navigator.mediaDevices && typeof navigator.mediaDevices.addEventListener === "function") {
        navigator.mediaDevices.addEventListener("devicechange", function () {
            refreshAudioDeviceOptions();
        });
    }

    root.addEventListener("click", function (event) {
        const tab = event.target.closest("[data-channel-tab]");
        if (!tab) return;
        event.stopPropagation();
        const nextChannel = tab.dataset.channelTab || "public";
        selectChannel(nextChannel);
    });

    els.sendBtn.addEventListener("click", sendChat);
    els.input.addEventListener("keydown", function (event) {
        if (event.key === "Enter") sendChat();
    });
    els.miniSendBtn.addEventListener("click", sendMiniChat);
    els.miniInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") sendMiniChat();
    });
    els.emojiBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        toggleEmojiPicker(els.emojiPicker);
    });
    els.miniEmojiBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        toggleEmojiPicker(els.miniEmojiPicker);
    });
    els.fxBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        toggleFxPicker(els.fxPicker);
    });
    els.miniFxBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        toggleFxPicker(els.miniFxPicker);
    });
    root.addEventListener("click", function (event) {
        const emojiItem = event.target.closest("[data-chat-emoji]");
        if (!emojiItem) return;
        event.stopPropagation();
        const emoji = emojiItem.dataset.chatEmoji || "";
        const isMini = !!(els.miniPanel && els.miniPanel.contains(emojiItem));
        appendEmojiToInput(isMini ? els.miniInput : els.input, emoji);
        closeEmojiPickers();
    });
    root.addEventListener("click", function (event) {
        const fxItem = event.target.closest("[data-screen-fx]");
        if (!fxItem) return;
        event.stopPropagation();
        sendScreenEffect(fxItem.dataset.screenFx || "");
    });
    root.addEventListener("click", function (event) {
        if (event.target.closest("[data-channel-tab]")) return;
        hideUnreadPopup();
    });
    els.messages.addEventListener("click", hideUnreadPopup);
    els.miniMessages.addEventListener("click", hideUnreadPopup);
    els.input.addEventListener("focus", hideUnreadPopup);
    els.miniInput.addEventListener("focus", hideUnreadPopup);
    els.popup.addEventListener("click", function () {
        hideUnreadPopup();
        selectChannel(getPreferredFocusChannel());
        if (viewMode === "compact") {
            openMiniChat();
            els.miniMessages.scrollIntoView({ behavior: "smooth", block: "center" });
            return;
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
    els.dockMicBtn.addEventListener("click", async function (event) {
        event.stopPropagation();
        if (!micEnabled) {
            await startVoice(false);
            return;
        }
        stopVoice(true);
    });
    els.dockListenBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        listenMuted = !listenMuted;
        saveListenMutedPreference(listenMuted);
        updateListenButton();
        applyListenMuteToAll();
        renderVoiceStatus();
        Array.from(els.volumeList.querySelectorAll("input[data-volume-device]")).forEach(function (input) {
            input.disabled = listenMuted;
        });
    });
    els.micSelect.addEventListener("change", async function () {
        selectedMicDeviceId = String(els.micSelect.value || "");
        saveMicDevicePreference(selectedMicDeviceId);
        if (micEnabled) {
            stopVoice(false);
            await startVoice(false);
        }
    });
    els.speakerSelect.addEventListener("change", function () {
        selectedSpeakerDeviceId = String(els.speakerSelect.value || "");
        saveSpeakerDevicePreference(selectedSpeakerDeviceId);
        applyOutputDeviceToAll();
    });
    els.dockChatBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        if (compactChatOpen) {
            closeMiniChat();
            return;
        }
        openMiniChat();
    });
    els.dockSettingBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        if (settingPanelOpen) {
            closeSettingPanel();
            return;
        }
        openSettingPanel();
    });
    els.viewModeToggle.addEventListener("change", function () {
        viewMode = els.viewModeToggle.checked ? "expanded" : "compact";
        saveViewModePreference(viewMode);
        applyViewMode();
    });
    document.addEventListener("click", function (event) {
        const target = event.target;
        if (target && target.closest && target.closest("[data-channel-tab]")) {
            return;
        }
        const clickInSettingPanel = !!(els.settingPanel && els.settingPanel.contains(target));
        const clickSettingButton = !!(els.dockSettingBtn && els.dockSettingBtn.contains(target));
        if (!clickInSettingPanel && !clickSettingButton) {
            closeSettingPanel();
        }
        const clickInChatPanel = !!(els.miniPanel && els.miniPanel.contains(target));
        const clickChatButton = !!(els.dockChatBtn && els.dockChatBtn.contains(target));
        if (!clickInChatPanel && !clickChatButton) {
            closeMiniChat();
        }
        const clickEmojiMain = !!(els.emojiWrap && els.emojiWrap.contains(target));
        const clickEmojiMini = !!(els.miniEmojiWrap && els.miniEmojiWrap.contains(target));
        if (!clickEmojiMain && !clickEmojiMini) {
            closeEmojiPickers();
        }
        const clickFxMain = !!(els.fxWrap && els.fxWrap.contains(target));
        const clickFxMini = !!(els.miniFxWrap && els.miniFxWrap.contains(target));
        if (!clickFxMain && !clickFxMini) {
            closeFxPickers();
        }
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
        ws = new WebSocket(protocol + location.host + "/stomp", "v12.stomp");
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
            const effectKey = parseScreenEffect(payload.message || "");
            if (effectKey) {
                playScreenEffect(effectKey);
                return;
            }
            if (!chatHistory[channel]) chatHistory[channel] = [];
            const line = (payload.senderName || "Ẩn danh") + ": " + (payload.message || "");
            chatHistory[channel].push(line);
            trimChatHistory(channel);
            saveChatHistory();
            const canReadNow = isReadingChannelNow(channel);
            if (!canReadNow) {
                unreadChannels[channel] = true;
                showUnreadPopup();
            }
            renderMessages();
            renderChannelTabs();
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
        currentChannel = channels.some(function (c) { return c.key === previous; })
            ? previous
            : (channels[0] ? channels[0].key : "public");
        renderChannelTabs();

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
        if (isReadingChannelNow(currentChannel)) {
            unreadChannels[currentChannel] = false;
            renderChannelTabs();
            updateUnreadPopupAndIcon();
        }
    }

    function refreshComposerState() {
        const channels = Array.isArray(state && state.channels) ? state.channels : [];
        const selected = channels.find(function (item) { return item.key === currentChannel; });
        const canSend = !!(selected && selected.canSend);
        els.input.disabled = !canSend;
        els.sendBtn.disabled = !canSend;
        els.miniInput.disabled = !canSend;
        els.miniSendBtn.disabled = !canSend;
        els.emojiBtn.disabled = !canSend;
        els.miniEmojiBtn.disabled = !canSend;
        els.fxBtn.disabled = !canSend;
        els.miniFxBtn.disabled = !canSend;
        els.input.placeholder = canSend ? "Nhập tin nhắn..." : "Kênh hiện tại chỉ đọc";
        els.miniInput.placeholder = canSend ? "Nhập tin nhắn..." : "Kênh hiện tại chỉ đọc";
        if (!canSend) {
            closeEmojiPickers();
            closeFxPickers();
        }
    }

    function renderMessages() {
        const list = chatHistory[currentChannel] || [];
        const html = list.map(function (line) {
            return '<div style="margin-bottom:4px;word-break:break-word;text-align:left;">' + escapeHtml(line) + "</div>";
        }).join("");
        els.messages.innerHTML = html;
        els.miniMessages.innerHTML = html;
        els.messages.scrollTop = els.messages.scrollHeight;
        els.miniMessages.scrollTop = els.miniMessages.scrollHeight;
    }

    function showUnreadPopup() {
        unreadPopupVisible = true;
        els.popup.style.display = "inline-flex";
        updateUnreadPopupAndIcon();
    }

    function hideUnreadPopup() {
        if (!unreadPopupVisible) return;
        unreadPopupVisible = false;
        els.popup.style.display = "none";
        updateUnreadPopupAndIcon();
    }

    function setChatUnreadIndicator(hasUnread) {
        els.dockChatBtn.classList.toggle("has-unread", !!hasUnread);
    }

    function applyViewMode() {
        const expanded = viewMode !== "compact";
        els.expandedWrap.style.display = expanded ? "" : "none";
        els.dockMicBtn.style.display = expanded ? "none" : "inline-flex";
        els.dockListenBtn.style.display = expanded ? "none" : "inline-flex";
        els.dockChatBtn.style.display = expanded ? "none" : "inline-flex";
        els.viewModeToggle.checked = expanded;
        els.viewModeLabel.textContent = expanded ? "Đang phóng to" : "Đang thu nhỏ";
        if (expanded) {
            unreadChannels[currentChannel] = false;
            renderChannelTabs();
            updateUnreadPopupAndIcon();
        }
        if (expanded) {
            closeMiniChat();
        }
    }

    function openMiniChat() {
        if (viewMode !== "compact") return;
        compactChatOpen = true;
        els.miniPanel.style.display = "block";
        els.dockChatBtn.classList.add("active");
        closeSettingPanel();
        unreadChannels[currentChannel] = false;
        renderChannelTabs();
        renderMessages();
        hideUnreadPopup();
    }

    function closeMiniChat() {
        compactChatOpen = false;
        els.miniPanel.style.display = "none";
        els.dockChatBtn.classList.remove("active");
    }

    function openSettingPanel() {
        settingPanelOpen = true;
        els.settingPanel.style.display = "block";
        els.dockSettingBtn.classList.add("active");
    }

    function closeSettingPanel() {
        settingPanelOpen = false;
        els.settingPanel.style.display = "none";
        els.dockSettingBtn.classList.remove("active");
    }

    function sendChat() {
        sendChatFromInput(els.input);
    }

    function sendMiniChat() {
        sendChatFromInput(els.miniInput);
    }

    function sendChatFromInput(inputEl) {
        if (!stompConnected || !state) return;
        const text = (inputEl.value || "").trim();
        if (!text) return;
        sendFrame("SEND", { destination: "/app/comm.chat", "content-type": "application/json" }, JSON.stringify({
            game: game,
            deviceId: deviceId,
            channel: currentChannel,
            message: text,
            senderName: myName
        }));
        els.input.value = "";
        els.miniInput.value = "";
        closeEmojiPickers();
        closeFxPickers();
    }

    function initEmojiPickers() {
        const html = chatEmojis.map(function (emoji) {
            return '<button type="button" class="comm-emoji-item" data-chat-emoji="' + escapeAttr(emoji) + '">' + escapeHtml(emoji) + "</button>";
        }).join("");
        els.emojiPicker.innerHTML = html;
        els.miniEmojiPicker.innerHTML = html;
    }

    function toggleEmojiPicker(pickerEl) {
        const willOpen = !pickerEl.classList.contains("open");
        closeEmojiPickers();
        if (willOpen) {
            pickerEl.classList.add("open");
        }
    }

    function closeEmojiPickers() {
        els.emojiPicker.classList.remove("open");
        els.miniEmojiPicker.classList.remove("open");
    }

    function initFxPickers() {
        const html = screenEffects.map(function (effect) {
            return '<button type="button" class="comm-fx-item" data-screen-fx="' + escapeAttr(effect.key) + '">' +
                '<span>' + escapeHtml(effect.icon) + "</span><span>" + escapeHtml(effect.label) + "</span></button>";
        }).join("");
        els.fxPicker.innerHTML = html;
        els.miniFxPicker.innerHTML = html;
    }

    function toggleFxPicker(pickerEl) {
        const willOpen = !pickerEl.classList.contains("open");
        closeFxPickers();
        closeEmojiPickers();
        if (willOpen) {
            pickerEl.classList.add("open");
        }
    }

    function closeFxPickers() {
        els.fxPicker.classList.remove("open");
        els.miniFxPicker.classList.remove("open");
    }

    function sendScreenEffect(effectKey) {
        const normalized = normalizeScreenEffect(effectKey);
        if (!normalized || !stompConnected || !state) {
            return;
        }
        const channels = Array.isArray(state.channels) ? state.channels : [];
        const selected = channels.find(function (item) { return item.key === currentChannel; });
        if (!(selected && selected.canSend)) {
            return;
        }
        sendFrame("SEND", { destination: "/app/comm.chat", "content-type": "application/json" }, JSON.stringify({
            game: game,
            deviceId: deviceId,
            channel: currentChannel,
            message: effectTokenPrefix + normalized,
            senderName: myName
        }));
        closeFxPickers();
        closeEmojiPickers();
    }

    function normalizeScreenEffect(effectKey) {
        const key = String(effectKey || "").trim();
        if (!key) return "";
        return screenEffects.some(function (effect) { return effect.key === key; }) ? key : "";
    }

    function parseScreenEffect(message) {
        const text = String(message || "");
        if (text.indexOf(effectTokenPrefix) !== 0) {
            return "";
        }
        return normalizeScreenEffect(text.substring(effectTokenPrefix.length));
    }

    function playScreenEffect(effectKey) {
        const normalized = normalizeScreenEffect(effectKey);
        if (!normalized || !els.fxLayer) {
            return;
        }
        if (normalized === "punch") {
            runPunchEffect();
            return;
        }
        if (normalized === "punchCombo") {
            runPunchComboEffect();
            return;
        }
        if (normalized === "heart") {
            runHeartEffect();
            return;
        }
        if (normalized === "ant") {
            runBugEffect("🐜");
            return;
        }
        if (normalized === "fly") {
            runBugEffect("🪰");
            return;
        }
        if (normalized === "lightning") {
            runLightningEffect();
            return;
        }
        if (normalized === "elephant") {
            runElephantEffect();
            return;
        }
        if (normalized === "fall") {
            runFallEffect();
        }
    }

    function runPunchEffect() {
        shakeScreen(480, false);
        const node = document.createElement("div");
        node.className = "comm-fx-punch";
        node.textContent = "👊";
        els.fxLayer.appendChild(node);
        window.setTimeout(function () {
            if (node.parentNode) node.parentNode.removeChild(node);
        }, 680);
    }

    function runPunchComboEffect() {
        shakeScreen(1900, true);
        const total = 12;
        const width = Math.max(320, window.innerWidth);
        const height = Math.max(240, window.innerHeight);
        for (let i = 0; i < total; i++) {
            window.setTimeout(function () {
                const node = document.createElement("div");
                node.className = "comm-fx-punch";
                node.textContent = "👊";
                const fontPx = Math.max(88, Math.min(width * 0.22, (width * (0.10 + Math.random() * 0.12))));
                const padX = fontPx * 0.55;
                const padY = fontPx * 0.55;
                const x = padX + Math.random() * Math.max(1, width - (padX * 2));
                const y = padY + Math.random() * Math.max(1, height - (padY * 2));
                node.style.left = x.toFixed(1) + "px";
                node.style.top = y.toFixed(1) + "px";
                node.style.fontSize = fontPx.toFixed(1) + "px";
                node.style.animationDuration = "1.9s";
                els.fxLayer.appendChild(node);
                window.setTimeout(function () {
                    if (node.parentNode) node.parentNode.removeChild(node);
                }, 2050);
            }, i * 140);
        }
    }

    function runHeartEffect() {
        const count = 44 + Math.floor(Math.random() * 22);
        for (let i = 0; i < count; i++) {
            const node = document.createElement("div");
            node.className = "comm-fx-heart";
            node.textContent = i % 3 === 0 ? "💖" : "❤️";
            node.style.left = (Math.random() * 100).toFixed(1) + "vw";
            node.style.setProperty("--drift", ((Math.random() - 0.5) * 220).toFixed(0) + "px");
            node.style.setProperty("--dur", (2.2 + Math.random() * 2.1).toFixed(2) + "s");
            els.fxLayer.appendChild(node);
            window.setTimeout(function () {
                if (node.parentNode) node.parentNode.removeChild(node);
            }, 4600);
        }
    }

    function runBugEffect(emoji) {
        const count = 3 + Math.floor(Math.random() * 3);
        const width = Math.max(320, window.innerWidth);
        const height = Math.max(240, window.innerHeight);
        const lifeMs = 12000;
        const bugSize = 34;
        for (let i = 0; i < count; i++) {
            const node = document.createElement("div");
            node.className = "comm-fx-bug";
            node.textContent = emoji;
            els.fxLayer.appendChild(node);
            let x = Math.random() * (width - bugSize);
            let y = Math.random() * (height - bugSize);
            let vx = (Math.random() * 12) - 6;
            let vy = (Math.random() * 12) - 6;
            node.style.left = x.toFixed(1) + "px";
            node.style.top = y.toFixed(1) + "px";
            const timer = window.setInterval(function () {
                x += vx;
                y += vy;
                if (x < 0 || x > width - bugSize) {
                    vx = -vx;
                    x = Math.max(0, Math.min(width - bugSize, x));
                }
                if (y < 0 || y > height - bugSize) {
                    vy = -vy;
                    y = Math.max(0, Math.min(height - bugSize, y));
                }
                if (Math.random() < 0.25) {
                    vx += (Math.random() - 0.5) * 2.8;
                    vy += (Math.random() - 0.5) * 2.8;
                    vx = Math.max(-8, Math.min(8, vx));
                    vy = Math.max(-8, Math.min(8, vy));
                }
                node.style.left = x.toFixed(1) + "px";
                node.style.top = y.toFixed(1) + "px";
                node.style.transform = "scale(" + (0.85 + Math.random() * 0.45).toFixed(2) + ") rotate(" + ((Math.random() * 40) - 20).toFixed(0) + "deg)";
            }, 110);
            window.setTimeout(function () {
                window.clearInterval(timer);
                if (node.parentNode) node.parentNode.removeChild(node);
            }, lifeMs);
        }
    }

    function runLightningEffect() {
        shakeScreen(700, false);
        playThunderSound(1);
        const width = Math.max(320, window.innerWidth);
        const height = Math.max(240, window.innerHeight);
        const baseImpactX = width * (0.24 + Math.random() * 0.52);
        const baseImpactY = height * (0.62 + Math.random() * 0.24);
        for (let i = 0; i < 3; i++) {
            window.setTimeout(function () {
                const flash = document.createElement("div");
                flash.className = "comm-fx-flash";
                flash.style.setProperty("--dur", (0.16 + Math.random() * 0.1).toFixed(2) + "s");
                els.fxLayer.appendChild(flash);

                const impactX = baseImpactX + ((Math.random() - 0.5) * 70);
                const impactY = baseImpactY + ((Math.random() - 0.5) * 34);
                const startX = impactX + ((Math.random() - 0.5) * 220);
                const strike = createLightningStrikeSvg(width, height, startX, impactX, impactY);
                els.fxLayer.appendChild(strike);

                const impact = document.createElement("div");
                impact.className = "comm-fx-impact";
                impact.style.left = impactX.toFixed(1) + "px";
                impact.style.top = impactY.toFixed(1) + "px";
                els.fxLayer.appendChild(impact);

                window.setTimeout(function () {
                    if (flash.parentNode) flash.parentNode.removeChild(flash);
                    if (strike.parentNode) strike.parentNode.removeChild(strike);
                    if (impact.parentNode) impact.parentNode.removeChild(impact);
                }, 680);
            }, i * 170);
        }
    }

    function createLightningStrikeSvg(width, height, startX, impactX, impactY) {
        const svgns = "http://www.w3.org/2000/svg";
        const svg = document.createElementNS(svgns, "svg");
        svg.setAttribute("class", "comm-fx-lightning-svg");
        svg.setAttribute("viewBox", "0 0 " + width + " " + height);
        svg.setAttribute("width", String(width));
        svg.setAttribute("height", String(height));

        const mainPathD = buildLightningPath(startX, 0, impactX, impactY, 12, 30);
        const mainGlow = document.createElementNS(svgns, "path");
        mainGlow.setAttribute("d", mainPathD);
        mainGlow.setAttribute("fill", "none");
        mainGlow.setAttribute("stroke", "rgba(255,255,250,0.95)");
        mainGlow.setAttribute("stroke-width", "5.2");
        mainGlow.setAttribute("stroke-linecap", "round");
        mainGlow.setAttribute("stroke-linejoin", "round");
        svg.appendChild(mainGlow);

        const mainCore = document.createElementNS(svgns, "path");
        mainCore.setAttribute("d", mainPathD);
        mainCore.setAttribute("fill", "none");
        mainCore.setAttribute("stroke", "rgba(147,197,253,0.95)");
        mainCore.setAttribute("stroke-width", "2.1");
        mainCore.setAttribute("stroke-linecap", "round");
        mainCore.setAttribute("stroke-linejoin", "round");
        svg.appendChild(mainCore);

        for (let i = 0; i < 2; i++) {
            const startT = 0.28 + (Math.random() * 0.34);
            const branchStartX = startX + ((impactX - startX) * startT);
            const branchStartY = impactY * startT;
            const branchEndX = branchStartX + ((Math.random() - 0.5) * 110);
            const branchEndY = branchStartY + (70 + Math.random() * 85);
            const branchD = buildLightningPath(branchStartX, branchStartY, branchEndX, branchEndY, 5, 16);
            const branch = document.createElementNS(svgns, "path");
            branch.setAttribute("d", branchD);
            branch.setAttribute("fill", "none");
            branch.setAttribute("stroke", "rgba(255,248,220,0.8)");
            branch.setAttribute("stroke-width", "2.4");
            branch.setAttribute("stroke-linecap", "round");
            branch.setAttribute("stroke-linejoin", "round");
            svg.appendChild(branch);
        }
        return svg;
    }

    function buildLightningPath(startX, startY, endX, endY, segments, jitterX) {
        const parts = ["M " + startX.toFixed(1) + " " + startY.toFixed(1)];
        for (let i = 1; i < segments; i++) {
            const t = i / segments;
            const y = startY + ((endY - startY) * t);
            const baseX = startX + ((endX - startX) * t);
            const x = baseX + ((Math.random() - 0.5) * jitterX * (1 - (t * 0.35)));
            parts.push("L " + x.toFixed(1) + " " + y.toFixed(1));
        }
        parts.push("L " + endX.toFixed(1) + " " + endY.toFixed(1));
        return parts.join(" ");
    }

    function playThunderSound(level) {
        const Ctx = window.AudioContext || window.webkitAudioContext;
        if (!Ctx) return;
        const volume = Math.max(0.01, Math.min(1, Number(level) || 0.04));
        let context = null;
        try {
            context = new Ctx();
            const now = context.currentTime;
            const master = context.createGain();
            master.gain.setValueAtTime(0.0001, now);
            master.gain.exponentialRampToValueAtTime(volume, now + 0.03);
            master.gain.exponentialRampToValueAtTime(volume * 0.42, now + 0.26);
            master.gain.exponentialRampToValueAtTime(0.0001, now + 1.15);
            master.connect(context.destination);

            const noiseBuffer = context.createBuffer(1, Math.floor(context.sampleRate * 1.2), context.sampleRate);
            const noiseData = noiseBuffer.getChannelData(0);
            for (let i = 0; i < noiseData.length; i++) {
                const t = i / noiseData.length;
                noiseData[i] = (Math.random() * 2 - 1) * (1 - (t * 0.72));
            }
            const noise = context.createBufferSource();
            noise.buffer = noiseBuffer;
            const lowPass = context.createBiquadFilter();
            lowPass.type = "lowpass";
            lowPass.frequency.setValueAtTime(360, now);
            lowPass.frequency.exponentialRampToValueAtTime(140, now + 1.1);
            const highPass = context.createBiquadFilter();
            highPass.type = "highpass";
            highPass.frequency.setValueAtTime(28, now);
            noise.connect(lowPass);
            lowPass.connect(highPass);
            highPass.connect(master);

            const rumble = context.createOscillator();
            rumble.type = "triangle";
            rumble.frequency.setValueAtTime(66, now);
            rumble.frequency.exponentialRampToValueAtTime(38, now + 0.95);
            const rumbleGain = context.createGain();
            rumbleGain.gain.setValueAtTime(volume * 0.34, now);
            rumbleGain.gain.exponentialRampToValueAtTime(0.0001, now + 1.05);
            rumble.connect(rumbleGain);
            rumbleGain.connect(master);

            noise.start(now);
            noise.stop(now + 1.2);
            rumble.start(now);
            rumble.stop(now + 1.08);
            window.setTimeout(function () {
                context.close().catch(function () {
                });
            }, 1400);
        } catch (_) {
            if (context) {
                context.close().catch(function () {
                });
            }
        }
    }

    function runElephantEffect() {
        const count = 8;
        const baseTop = 10;
        const rangeTop = 72;
        for (let i = 0; i < count; i++) {
            const node = document.createElement("div");
            node.className = "comm-fx-elephant";
            node.textContent = "🐘";
            node.style.top = (baseTop + Math.random() * rangeTop).toFixed(1) + "vh";
            node.style.fontSize = (42 + Math.random() * 26).toFixed(0) + "px";
            node.style.setProperty("--delay", (i * 0.34).toFixed(2) + "s");
            node.style.setProperty("--dur", (7.5 + Math.random() * 2.5).toFixed(2) + "s");
            els.fxLayer.appendChild(node);
            window.setTimeout(function () {
                if (node.parentNode) node.parentNode.removeChild(node);
            }, 10800 + (i * 200));
        }
    }

    function runFallEffect() {
        const body = document.body;
        if (!body) return;
        body.classList.remove("comm-fall-mode");
        if (fallRestoreTimer) {
            window.clearTimeout(fallRestoreTimer);
            fallRestoreTimer = 0;
        }
        const pieces = Array.from(body.querySelectorAll("*")).filter(function (el) {
            if (!el || !el.tagName) return false;
            const tag = String(el.tagName).toUpperCase();
            if (tag === "SCRIPT" || tag === "STYLE" || tag === "META" || tag === "LINK") return false;
            if (el.id === "commFxLayer") return false;
            if (el.closest && el.closest("#commFxLayer")) return false;
            return true;
        });
        pieces.forEach(function (el) {
            el.classList.remove("comm-fall-piece");
            el.style.removeProperty("--fall-delay");
            el.style.removeProperty("--fall-drift");
            el.style.removeProperty("--fall-drop");
            el.style.removeProperty("--fall-rot");
            void el.offsetWidth;
            el.classList.add("comm-fall-piece");
            el.style.setProperty("--fall-delay", (Math.random() * 1.25).toFixed(2) + "s");
            el.style.setProperty("--fall-drift", (((Math.random() - 0.5) * 260)).toFixed(0) + "px");
            el.style.setProperty("--fall-drop", (28 + (Math.random() * 44)).toFixed(1) + "vh");
            el.style.setProperty("--fall-rot", (((Math.random() - 0.5) * 26)).toFixed(1) + "deg");
        });
        void body.offsetWidth;
        body.classList.add("comm-fall-mode");
        fallRestoreTimer = window.setTimeout(function () {
            body.classList.remove("comm-fall-mode");
            pieces.forEach(function (el) {
                el.classList.remove("comm-fall-piece");
                el.style.removeProperty("--fall-delay");
                el.style.removeProperty("--fall-drift");
                el.style.removeProperty("--fall-drop");
                el.style.removeProperty("--fall-rot");
            });
            fallRestoreTimer = 0;
        }, 5800);
    }

    function shakeScreen(durationMs, hardMode) {
        const body = document.body;
        if (!body) return;
        body.classList.remove("comm-screen-shake");
        body.classList.remove("comm-screen-shake-hard");
        if (screenShakeTimer) {
            window.clearTimeout(screenShakeTimer);
            screenShakeTimer = 0;
        }
        void body.offsetWidth;
        body.classList.add(hardMode ? "comm-screen-shake-hard" : "comm-screen-shake");
        screenShakeTimer = window.setTimeout(function () {
            body.classList.remove("comm-screen-shake");
            body.classList.remove("comm-screen-shake-hard");
            screenShakeTimer = 0;
        }, Math.max(200, Number(durationMs) || 480));
    }

    function appendEmojiToInput(inputEl, emoji) {
        if (!emoji || inputEl.disabled) return;
        const current = String(inputEl.value || "");
        inputEl.value = (current + emoji).slice(0, 220);
        inputEl.focus();
    }

    function getChannels() {
        return Array.isArray(state && state.channels) ? state.channels : [];
    }

    function channelDisplayTitle(channel) {
        const title = channel && channel.title ? String(channel.title) : "";
        return title || "Kênh";
    }

    function renderChannelTabs() {
        const channels = getChannels();
        const hasManyChannels = channels.length > 1;
        [els.channelTabs, els.miniChannelTabs].forEach(function (holder) {
            if (!holder) return;
            holder.style.display = hasManyChannels ? "flex" : "none";
            if (!hasManyChannels) {
                holder.innerHTML = "";
                return;
            }
            holder.innerHTML = channels.map(function (channel) {
                const key = channel.key;
                const activeClass = key === currentChannel ? " active" : "";
                const unreadClass = unreadChannels[key] ? " has-unread" : "";
                const label = channelDisplayTitle(channel);
                return '<button type="button" class="comm-channel-tab' + activeClass + unreadClass + '" data-channel-tab="' + escapeAttr(key) + '">' + escapeHtml(label) + "</button>";
            }).join("");
        });
    }

    function selectChannel(channelKey) {
        const channels = getChannels();
        const exists = channels.some(function (item) { return item.key === channelKey; });
        currentChannel = exists ? channelKey : (channels[0] ? channels[0].key : "public");
        if (isReadingChannelNow(currentChannel)) {
            unreadChannels[currentChannel] = false;
        }
        renderChannelTabs();
        refreshComposerState();
        renderMessages();
        updateUnreadPopupAndIcon();
    }

    function getPreferredFocusChannel() {
        const channels = getChannels();
        const unread = channels.find(function (channel) { return unreadChannels[channel.key]; });
        if (unread) return unread.key;
        const hasPublic = channels.some(function (channel) { return channel.key === "public"; });
        if (hasPublic) return "public";
        return channels[0] ? channels[0].key : "public";
    }

    function isReadingChannelNow(channelKey) {
        if (channelKey !== currentChannel) return false;
        if (viewMode === "compact") return compactChatOpen;
        return true;
    }

    function hasAnyUnreadChannel() {
        const channels = getChannels();
        return channels.some(function (channel) { return !!unreadChannels[channel.key]; });
    }

    function updateUnreadPopupAndIcon() {
        const hasUnread = hasAnyUnreadChannel();
        if (!hasUnread) {
            unreadPopupVisible = false;
            els.popup.style.display = "none";
        } else if (unreadPopupVisible) {
            els.popup.style.display = "inline-flex";
        }
        setChatUnreadIndicator(hasUnread);
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
        applyOutputDevice(audio);
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

    function loadChatHistory() {
        try {
            const parsed = JSON.parse(localStorage.getItem(chatHistoryStorageKey) || "{}");
            const normalized = {};
            if (!parsed || typeof parsed !== "object") {
                normalized.public = [];
                normalized.dead = [];
                return normalized;
            }
            Object.keys(parsed).forEach(function (key) {
                const list = Array.isArray(parsed[key]) ? parsed[key] : [];
                normalized[key] = list
                    .filter(function (item) { return typeof item === "string"; })
                    .slice(-maxChatPerChannel);
            });
            if (!normalized.public) normalized.public = [];
            if (!normalized.dead) normalized.dead = [];
            return normalized;
        } catch (_) {
            return { public: [], dead: [] };
        }
    }

    function trimChatHistory(channel) {
        if (!chatHistory[channel]) return;
        if (chatHistory[channel].length > maxChatPerChannel) {
            chatHistory[channel] = chatHistory[channel].slice(-maxChatPerChannel);
        }
    }

    function saveChatHistory() {
        localStorage.setItem(chatHistoryStorageKey, JSON.stringify(chatHistory));
    }

    function loadViewModePreference() {
        return localStorage.getItem(viewModeStorageKey) === "compact" ? "compact" : "expanded";
    }

    function saveViewModePreference(nextMode) {
        localStorage.setItem(viewModeStorageKey, nextMode === "compact" ? "compact" : "expanded");
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

    function loadMicDevicePreference() {
        return localStorage.getItem(micDeviceStorageKey) || "";
    }

    function saveMicDevicePreference(deviceIdValue) {
        localStorage.setItem(micDeviceStorageKey, deviceIdValue || "");
    }

    function loadListenMutedPreference() {
        return localStorage.getItem(listenMutedStorageKey) === "1";
    }

    function saveListenMutedPreference(enabled) {
        localStorage.setItem(listenMutedStorageKey, enabled ? "1" : "0");
    }

    function loadSpeakerDevicePreference() {
        return localStorage.getItem(speakerDeviceStorageKey) || "";
    }

    function saveSpeakerDevicePreference(deviceIdValue) {
        localStorage.setItem(speakerDeviceStorageKey, deviceIdValue || "");
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
        refreshAudioDeviceOptions();
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
            const audioConstraint = selectedMicDeviceId ? { deviceId: { exact: selectedMicDeviceId } } : true;
            return navigator.mediaDevices.getUserMedia({ audio: audioConstraint, video: false });
        }
        const legacyGetUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
        if (typeof legacyGetUserMedia === "function") {
            return new Promise(function (resolve, reject) {
                legacyGetUserMedia.call(navigator, { audio: true, video: false }, resolve, reject);
            });
        }
        return null;
    }

    async function refreshAudioDeviceOptions() {
        if (!navigator.mediaDevices || typeof navigator.mediaDevices.enumerateDevices !== "function") {
            updateDeviceSelects([], []);
            return;
        }
        try {
            const devices = await navigator.mediaDevices.enumerateDevices();
            const micDevices = devices.filter(function (device) { return device.kind === "audioinput"; });
            const speakerDevices = devices.filter(function (device) { return device.kind === "audiooutput"; });
            updateDeviceSelects(micDevices, speakerDevices);
        } catch (_) {
            updateDeviceSelects([], []);
        }
    }

    function updateDeviceSelects(micDevices, speakerDevices) {
        populateDeviceSelect(els.micSelect, micDevices, selectedMicDeviceId, "Micro mặc định", "Mic");
        selectedMicDeviceId = String(els.micSelect.value || "");
        saveMicDevicePreference(selectedMicDeviceId);

        if (!canSetSinkId) {
            populateFixedSelect(els.speakerSelect, "Trình duyệt không hỗ trợ chọn loa");
            els.speakerSelect.disabled = true;
            return;
        }

        populateDeviceSelect(els.speakerSelect, speakerDevices, selectedSpeakerDeviceId, "Loa mặc định", "Loa");
        selectedSpeakerDeviceId = String(els.speakerSelect.value || "");
        saveSpeakerDevicePreference(selectedSpeakerDeviceId);
        els.speakerSelect.disabled = false;
        applyOutputDeviceToAll();
    }

    function populateDeviceSelect(selectEl, devices, selectedId, defaultLabel, fallbackPrefix) {
        if (!selectEl) return;
        selectEl.innerHTML = "";
        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = defaultLabel;
        selectEl.appendChild(defaultOption);

        devices.forEach(function (device, index) {
            const option = document.createElement("option");
            option.value = device.deviceId || "";
            option.textContent = device.label || (fallbackPrefix + " " + (index + 1));
            selectEl.appendChild(option);
        });

        const preferred = String(selectedId || "");
        const hasPreferred = Array.from(selectEl.options).some(function (option) {
            return option.value === preferred;
        });
        selectEl.value = hasPreferred ? preferred : "";
    }

    function populateFixedSelect(selectEl, label) {
        if (!selectEl) return;
        selectEl.innerHTML = "";
        const option = document.createElement("option");
        option.value = "";
        option.textContent = label;
        selectEl.appendChild(option);
        selectEl.value = "";
    }

    function applyOutputDevice(audioElement) {
        if (!audioElement || !canSetSinkId || typeof audioElement.setSinkId !== "function") {
            return;
        }
        audioElement.setSinkId(selectedSpeakerDeviceId || "").catch(function () {
        });
    }

    function applyOutputDeviceToAll() {
        Array.from(audioMap.values()).forEach(function (audio) {
            applyOutputDevice(audio);
        });
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
            els.dockMicBtn.textContent = "🎤";
            els.dockMicBtn.title = "Tắt mic";
            els.dockMicBtn.classList.add("state-on");
            els.dockMicBtn.classList.remove("state-off");
        } else {
            els.voiceBtn.textContent = "Bật Mic";
            els.voiceBtn.classList.remove("btn-success");
            els.voiceBtn.classList.add("btn-danger");
            els.dockMicBtn.textContent = "🎙️";
            els.dockMicBtn.title = "Bật mic";
            els.dockMicBtn.classList.add("state-off");
            els.dockMicBtn.classList.remove("state-on");
        }
    }

    function updateListenButton() {
        if (listenMuted) {
            els.listenBtn.textContent = "Bật âm";
            els.listenBtn.classList.remove("btn-primary");
            els.listenBtn.classList.add("btn-danger");
            els.dockListenBtn.textContent = "🔇";
            els.dockListenBtn.title = "Bật âm";
            els.dockListenBtn.classList.add("state-off");
            els.dockListenBtn.classList.remove("state-on");
        } else {
            els.listenBtn.textContent = "Tắt âm";
            els.listenBtn.classList.remove("btn-danger");
            els.listenBtn.classList.add("btn-primary");
            els.dockListenBtn.textContent = "🔊";
            els.dockListenBtn.title = "Tắt âm";
            els.dockListenBtn.classList.add("state-on");
            els.dockListenBtn.classList.remove("state-off");
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

    function escapeAttr(text) {
        return escapeHtml(text).replace(/`/g, "&#96;");
    }
})();
