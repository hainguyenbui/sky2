/* ============================================================
   Party Games Hub - lớp giao tiếp với người dùng
   Thay cho alert() / confirm() của trình duyệt.

   UI.toast(message, tone)        thông báo thoáng qua, tự biến mất
   UI.notify({title, message})    kết quả quan trọng, người dùng phải tự đóng
   UI.confirm({title, message})   hỏi xác nhận, trả về Promise<boolean>
   UI.flash({...})                giữ thông báo qua một lần tải lại trang
   UI.reload(), UI.go(url)        tải lại / chuyển trang sau khi đã đặt flash

   tone: 'info' | 'success' | 'danger'
   ============================================================ */

(function () {
    'use strict';

    const FLASH_KEY = 'ui.flash.v1';
    const TOAST_DURATION = 4200;

    /* ---------- Toast ---------- */

    let toastRegion = null;

    function region() {
        if (!toastRegion) {
            toastRegion = document.createElement('div');
            toastRegion.className = 'toast-region';
            toastRegion.setAttribute('role', 'status');
            toastRegion.setAttribute('aria-live', 'polite');
            document.body.appendChild(toastRegion);
        }
        return toastRegion;
    }

    function toast(message, tone) {
        if (!message) return;
        const el = document.createElement('div');
        el.className = 'toast toast-' + (tone || 'info');
        el.textContent = message;
        region().appendChild(el);

        // Buộc trình duyệt tính layout trước khi thêm class chạy hiệu ứng vào
        void el.offsetWidth;
        el.classList.add('is-visible');

        const remove = () => {
            el.classList.remove('is-visible');
            el.addEventListener('transitionend', () => el.remove(), { once: true });
            // Phòng khi transition không chạy (tab ẩn, prefers-reduced-motion)
            window.setTimeout(() => el.remove(), 400);
        };
        window.setTimeout(remove, TOAST_DURATION);
        el.addEventListener('click', remove);
    }

    /* ---------- Hộp thoại ---------- */

    // Kết quả được quyết định ngay trong handler của nút, không chờ sự kiện
    // 'close' của <dialog>: sự kiện đó không phát ổn định trên mọi trang.
    function openDialog({ title, message, tone, confirmLabel, cancelLabel }) {
        return new Promise(resolve => {
            const dialog = document.createElement('dialog');
            dialog.className = 'dialog';

            let settled = false;
            function settle(confirmed) {
                if (settled) return;
                settled = true;
                if (dialog.open) dialog.close();
                dialog.remove();
                resolve(confirmed);
            }

            const heading = document.createElement('h2');
            heading.className = 'dialog-title';
            heading.textContent = title;

            const actions = document.createElement('div');
            actions.className = 'dialog-actions';

            if (cancelLabel) {
                const cancel = document.createElement('button');
                cancel.type = 'button';
                cancel.className = 'btn btn-secondary';
                cancel.textContent = cancelLabel;
                cancel.addEventListener('click', () => settle(false));
                actions.appendChild(cancel);
            }

            const confirm = document.createElement('button');
            confirm.type = 'button';
            confirm.className = 'btn ' + (tone === 'danger' ? 'btn-danger' : 'btn-primary');
            confirm.textContent = confirmLabel;
            confirm.addEventListener('click', () => settle(true));
            actions.appendChild(confirm);

            dialog.append(heading);
            if (message) {
                const body = document.createElement('p');
                body.className = 'dialog-message';
                body.textContent = message;
                dialog.append(body);
            }
            dialog.append(actions);

            // Escape: coi như huỷ, giống hành vi của confirm()
            dialog.addEventListener('cancel', event => {
                event.preventDefault();
                settle(false);
            });

            document.body.appendChild(dialog);
            dialog.showModal();
            confirm.focus();
        });
    }

    function confirmAction({ title, message, confirmLabel, cancelLabel, tone }) {
        return openDialog({
            title: title,
            message: message,
            tone: tone,
            confirmLabel: confirmLabel || 'Xác nhận',
            cancelLabel: cancelLabel || 'Huỷ'
        });
    }

    function notify({ title, message, tone, confirmLabel }) {
        return openDialog({
            title: title,
            message: message,
            tone: tone,
            confirmLabel: confirmLabel || 'Đã hiểu',
            cancelLabel: null
        });
    }

    /* ---------- Thông báo sống sót qua lần tải lại ---------- */

    function flash(payload) {
        try {
            sessionStorage.setItem(FLASH_KEY, JSON.stringify(payload));
        } catch (error) {
            console.debug('Không lưu được thông báo:', error);
        }
    }

    function drainFlash() {
        let raw;
        try {
            raw = sessionStorage.getItem(FLASH_KEY);
            sessionStorage.removeItem(FLASH_KEY);
        } catch (error) {
            return;
        }
        if (!raw) return;
        try {
            const payload = JSON.parse(raw);
            if (payload.title) notify(payload);
            else toast(payload.message, payload.tone);
        } catch (error) {
            console.debug('Thông báo lưu lại bị hỏng:', error);
        }
    }

    function reload() {
        window.location.reload();
    }

    function go(url) {
        window.location.href = url;
    }

    /* ---------- Tô đậm tên người chơi trong dòng sự kiện ---------- */

    function escapeRegExp(value) {
        return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    function namePattern(names) {
        // Tên dài trước để "Bình 2" không bị "Bình" ăn mất phần đuôi.
        const sorted = names.filter(Boolean).slice().sort((a, b) => b.length - a.length);
        if (!sorted.length) return null;
        const body = sorted.map(escapeRegExp).join('|');
        // Nhóm 1 là ký tự đứng trước, giữ nguyên khi ghép lại. Dùng lookahead
        // thay cho lookbehind để chạy được trên cả trình duyệt cũ.
        return new RegExp('(^|[^\\p{L}\\p{N}])(' + body + ')(?![\\p{L}\\p{N}])', 'gu');
    }

    function highlightNames(root, names) {
        const pattern = namePattern(names || []);
        if (!root || !pattern) return;

        const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
            acceptNode(node) {
                if (!node.nodeValue.trim()) return NodeFilter.FILTER_REJECT;
                // Không đụng vào phần đã tô, tránh lồng nhau khi gọi lại
                if (node.parentElement.closest('.name-token')) return NodeFilter.FILTER_REJECT;
                return NodeFilter.FILTER_ACCEPT;
            }
        });

        const textNodes = [];
        while (walker.nextNode()) textNodes.push(walker.currentNode);

        textNodes.forEach(node => {
            const text = node.nodeValue;
            pattern.lastIndex = 0;
            if (!pattern.test(text)) return;
            pattern.lastIndex = 0;

            const fragment = document.createDocumentFragment();
            let cursor = 0;
            let match;
            while ((match = pattern.exec(text)) !== null) {
                const nameStart = match.index + match[1].length;
                if (nameStart > cursor) {
                    fragment.append(text.slice(cursor, nameStart));
                }
                const token = document.createElement('span');
                token.className = 'name-token';
                token.textContent = match[2];
                fragment.append(token);
                cursor = nameStart + match[2].length;
            }
            if (cursor < text.length) fragment.append(text.slice(cursor));
            node.parentNode.replaceChild(fragment, node);
        });
    }

    /* ---------- Thẻ lật giấu vai trò ---------- */

    function wireRevealCards() {
        document.querySelectorAll('[data-reveal]').forEach(box => {
            box.setAttribute('role', 'button');
            box.setAttribute('tabindex', '0');

            const sync = () => {
                const hidden = box.classList.contains('blurred');
                box.setAttribute('aria-pressed', String(!hidden));
                box.setAttribute('aria-label', hidden ? 'Chạm để xem thông tin bí mật' : 'Chạm để ẩn');
            };

            const toggle = () => {
                box.classList.toggle('blurred');
                sync();
            };

            box.addEventListener('click', toggle);
            box.addEventListener('keydown', event => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    toggle();
                }
            });
            sync();
        });

        // Tự che lại khi rời khỏi trang: khoá máy hay chuyển app
        // thì không để lộ vai trò cho người ngồi cạnh.
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState !== 'hidden') return;
            document.querySelectorAll('[data-reveal]').forEach(box => {
                box.classList.add('blurred');
                box.setAttribute('aria-pressed', 'false');
                box.setAttribute('aria-label', 'Chạm để xem thông tin bí mật');
            });
        });
    }

    window.UI = {
        toast: toast,
        notify: notify,
        confirm: confirmAction,
        flash: flash,
        reload: reload,
        go: go,
        highlightNames: highlightNames
    };

    document.addEventListener('DOMContentLoaded', function () {
        wireRevealCards();
        drainFlash();
    });
})();
