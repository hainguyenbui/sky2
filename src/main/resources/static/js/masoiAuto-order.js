(function() {
    var allKnownPlayers = {};
    var viewerDeviceOrder = {};
    var orderPlayersMeta = [];
    var orderDraftDeviceIds = [];
    var draggingOrderDeviceId = '';
    var cfg = {
        orderApiBase: '/msAuto/order',
        templateUrl: '/msAutoV1/showOrderEditor',
        onOrderUpdated: null,
        handleJsonResponse: null
    };

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function escapeAttr(value) {
        return escapeHtml(value).replace(/`/g, '&#96;');
    }

    function normalizeOrderMap(source) {
        var normalized = {};
        if (!source || typeof source !== 'object') {
            return normalized;
        }
        Object.keys(source).forEach(function(deviceId) {
            var raw = parseInt(source[deviceId], 10);
            if (!Number.isNaN(raw)) {
                normalized[String(deviceId)] = raw;
            }
        });
        return normalized;
    }

    function parsePlayersMeta(players) {
        return Array.isArray(players) ? players.map(function(player) {
            return {
                deviceId: String((player && player.deviceId) || ''),
                name: (player && player.name) || 'Ẩn danh',
                isDead: !!(player && player.isDead)
            };
        }).filter(function(player) { return !!player.deviceId; }) : [];
    }

    function applyViewerOrder(players) {
        if (!Array.isArray(players) || !players.length) {
            return [];
        }
        if (!Object.keys(viewerDeviceOrder || {}).length) {
            return players;
        }
        return players
            .map(function(player, index) {
                return {
                    deviceId: player.deviceId,
                    name: player.name,
                    isDead: !!player.isDead,
                    baseIndex: index,
                    customOrder: viewerDeviceOrder[player.deviceId]
                };
            })
            .sort(function(a, b) {
                var aHas = Number.isInteger(a.customOrder);
                var bHas = Number.isInteger(b.customOrder);
                if (aHas && bHas && a.customOrder !== b.customOrder) {
                    return a.customOrder - b.customOrder;
                }
                if (aHas !== bHas) {
                    return aHas ? -1 : 1;
                }
                return a.baseIndex - b.baseIndex;
            })
            .map(function(player) {
                return {
                    deviceId: player.deviceId,
                    name: player.name,
                    isDead: player.isDead
                };
            });
    }

    function mergeKnownPlayers(playersMap) {
        if (!playersMap || typeof playersMap !== 'object') {
            return;
        }
        Object.keys(playersMap).forEach(function(deviceId) {
            allKnownPlayers[String(deviceId)] = playersMap[deviceId];
        });
    }

    function updateDeadFlags(deadDeviceIds) {
        if (!Array.isArray(orderPlayersMeta)) {
            return;
        }
        var deadSet = new Set((deadDeviceIds || []).map(function(id) { return String(id); }));
        orderPlayersMeta = orderPlayersMeta.map(function(player) {
            return {
                deviceId: player.deviceId,
                name: player.name,
                isDead: deadSet.has(player.deviceId)
            };
        });
    }

    function orderPlayersForEditor() {
        if (Array.isArray(orderPlayersMeta) && orderPlayersMeta.length) {
            return applyViewerOrder(orderPlayersMeta);
        }
        var deadSet = new Set(orderPlayersMeta.filter(function(item) { return item.isDead; }).map(function(item) { return item.deviceId; }));
        var fallback = Object.keys(allKnownPlayers || {}).map(function(deviceId) {
            return {
                deviceId: deviceId,
                name: allKnownPlayers[deviceId] || 'Ẩn danh',
                isDead: deadSet.has(deviceId)
            };
        });
        return applyViewerOrder(fallback);
    }

    function renderOrderModalList() {
        var list = document.getElementById('orderPlayerList');
        if (!list) {
            return;
        }
        var metaById = {};
        orderPlayersForEditor().forEach(function(player) {
            metaById[player.deviceId] = player;
        });
        list.innerHTML = orderDraftDeviceIds.map(function(deviceId, index) {
            var player = metaById[deviceId];
            if (!player) {
                return '';
            }
            return '<div class="order-item' + (player.isDead ? ' dead' : '') + '" draggable="true" data-order-device="' + escapeAttr(deviceId) + '">' +
                '<div><div class="order-item-name">' + (index + 1) + '. ' + escapeHtml(player.name || 'Ẩn danh') + '</div></div>' +
                (player.isDead ? '<span class="order-item-badge">Bị loại</span>' : '<span class="order-item-meta">Đang chơi</span>') +
            '</div>';
        }).join('');
    }

    function openOrderModal() {
        var players = orderPlayersForEditor();
        orderDraftDeviceIds = players.map(function(player) { return player.deviceId; });
        renderOrderModalList();
        var modal = document.getElementById('orderModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    function closeOrderModal() {
        var modal = document.getElementById('orderModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }

    function callOnUpdated() {
        if (typeof cfg.onOrderUpdated === 'function') {
            cfg.onOrderUpdated();
        }
    }

    function defaultJsonResponse(response) {
        if (!response.ok) {
            throw new Error('HTTP ' + response.status);
        }
        return response.json();
    }

    function postJson(url, body) {
        return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        }).then(cfg.handleJsonResponse || defaultJsonResponse);
    }

    function saveOrderDraft(closeModal) {
        var deviceOrder = {};
        orderDraftDeviceIds.forEach(function(deviceId, index) {
            deviceOrder[deviceId] = index;
        });
        // Vấn đề 19: bấm sort thì lưu luôn order mới vào backend theo yêu cầu.
        postJson(cfg.orderApiBase, { deviceOrder: deviceOrder })
            .then(function(payload) {
                viewerDeviceOrder = normalizeOrderMap(payload.deviceOrder || {});
                orderPlayersMeta = parsePlayersMeta(payload.players);
                mergeKnownPlayers(orderPlayersMeta.reduce(function(acc, player) {
                    acc[player.deviceId] = player.name;
                    return acc;
                }, {}));
                callOnUpdated();
                if (closeModal !== false) {
                    closeOrderModal();
                }
            })
            .catch(function(error) {
                alert(error.message || 'Không lưu được thứ tự');
            });
    }

    function sortOrderDraftByName(ascending) {
        var players = orderPlayersForEditor();
        var byId = {};
        players.forEach(function(player) { byId[player.deviceId] = player; });
        orderDraftDeviceIds.sort(function(a, b) {
            var nameA = ((byId[a] && byId[a].name) || '').toLowerCase();
            var nameB = ((byId[b] && byId[b].name) || '').toLowerCase();
            var compare = nameA.localeCompare(nameB, 'vi');
            return ascending ? compare : -compare;
        });
        renderOrderModalList();
        saveOrderDraft(false);
    }

    function moveDraftOrderItem(fromDeviceId, toDeviceId) {
        if (!fromDeviceId || !toDeviceId || fromDeviceId === toDeviceId) {
            return;
        }
        var fromIndex = orderDraftDeviceIds.indexOf(fromDeviceId);
        var toIndex = orderDraftDeviceIds.indexOf(toDeviceId);
        if (fromIndex < 0 || toIndex < 0) {
            return;
        }
        var moved = orderDraftDeviceIds.splice(fromIndex, 1)[0];
        orderDraftDeviceIds.splice(toIndex, 0, moved);
        renderOrderModalList();
    }

    function bindOrderModalEvents() {
        var closeBtn = document.getElementById('orderCloseBtn');
        if (closeBtn) {
            closeBtn.addEventListener('click', closeOrderModal);
        }
        var saveBtn = document.getElementById('orderSaveBtn');
        if (saveBtn) {
            saveBtn.addEventListener('click', function() { saveOrderDraft(true); });
        }
        var sortAscBtn = document.getElementById('orderSortAscBtn');
        if (sortAscBtn) {
            sortAscBtn.addEventListener('click', function() { sortOrderDraftByName(true); });
        }
        var sortDescBtn = document.getElementById('orderSortDescBtn');
        if (sortDescBtn) {
            sortDescBtn.addEventListener('click', function() { sortOrderDraftByName(false); });
        }
        var orderModal = document.getElementById('orderModal');
        if (orderModal) {
            orderModal.addEventListener('click', function(event) {
                if (event.target === orderModal) {
                    closeOrderModal();
                }
            });
        }
        var orderList = document.getElementById('orderPlayerList');
        if (!orderList) {
            return;
        }
        orderList.addEventListener('dragstart', function(event) {
            var item = event.target.closest('.order-item');
            if (!item) {
                return;
            }
            draggingOrderDeviceId = item.dataset.orderDevice || '';
            item.classList.add('dragging');
            if (event.dataTransfer) {
                event.dataTransfer.effectAllowed = 'move';
            }
        });
        orderList.addEventListener('dragend', function(event) {
            var item = event.target.closest('.order-item');
            if (item) {
                item.classList.remove('dragging');
            }
            draggingOrderDeviceId = '';
        });
        orderList.addEventListener('dragover', function(event) {
            event.preventDefault();
            if (event.dataTransfer) {
                event.dataTransfer.dropEffect = 'move';
            }
        });
        orderList.addEventListener('drop', function(event) {
            event.preventDefault();
            var item = event.target.closest('.order-item');
            if (!item) {
                return;
            }
            var dropDeviceId = item.dataset.orderDevice || '';
            moveDraftOrderItem(draggingOrderDeviceId, dropDeviceId);
        });
    }

    function loadOrderModalTemplate() {
        var host = document.getElementById('orderModalHost');
        if (!host) {
            return Promise.reject(new Error('Không tìm thấy vùng order modal'));
        }
        return fetch(cfg.templateUrl, { method: 'POST' })
            .then(function(response) { return response.text(); })
            .then(function(html) {
                host.innerHTML = html;
                bindOrderModalEvents();
            });
    }

    function loadOrderConfig() {
        return fetch(cfg.orderApiBase)
            .then(cfg.handleJsonResponse || defaultJsonResponse)
            .then(function(payload) {
                viewerDeviceOrder = normalizeOrderMap(payload.deviceOrder || {});
                orderPlayersMeta = parsePlayersMeta(payload.players);
                mergeKnownPlayers(orderPlayersMeta.reduce(function(acc, player) {
                    acc[player.deviceId] = player.name;
                    return acc;
                }, {}));
                callOnUpdated();
            })
            .catch(function() {
                viewerDeviceOrder = {};
            });
    }

    function init(options) {
        cfg = Object.assign({}, cfg, options || {});
        loadOrderConfig();
        var editBtn = document.getElementById('editOrderBtn');
        if (editBtn) {
            editBtn.addEventListener('click', function() {
                loadOrderModalTemplate()
                    .then(openOrderModal)
                    .catch(function(error) {
                        alert(error.message || 'Không mở được popup sắp xếp');
                    });
            });
        }
    }

    window.MaSoiOrder = {
        init: init,
        applyViewerOrder: applyViewerOrder,
        mergeKnownPlayers: mergeKnownPlayers,
        updateDeadFlags: updateDeadFlags,
        getOrderState: function() {
            return Object.assign({}, viewerDeviceOrder);
        }
    };
})();
