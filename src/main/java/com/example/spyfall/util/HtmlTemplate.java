package com.example.spyfall.util;

public class HtmlTemplate {
    
    public static String wrap(String title, String content) {
        return wrap(title, content, "");
    }

    public static String wrap(String title, String content, String additionalHead) {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"vi\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
               "    <title>" + title + "</title>\n" +
               "    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
               "    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
               "    <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n" +
               "    <style>\n" +
               "        :root {\n" +
               "            --bg-gradient: linear-gradient(135deg, #090d16 0%, #111827 100%);\n" +
               "            --card-bg: rgba(30, 41, 59, 0.7);\n" +
               "            --card-border: rgba(255, 255, 255, 0.08);\n" +
               "            --text-primary: #f8fafc;\n" +
               "            --text-muted: #94a3b8;\n" +
               "            --primary: #3b82f6;\n" +
               "            --primary-hover: #2563eb;\n" +
               "            --danger: #ef4444;\n" +
               "            --danger-hover: #dc2626;\n" +
               "            --warning: #f59e0b;\n" +
               "            --warning-hover: #d97706;\n" +
               "            --success: #10b981;\n" +
               "            --success-hover: #059669;\n" +
               "        }\n" +
               "        \n" +
               "        * {\n" +
               "            box-sizing: border-box;\n" +
               "            margin: 0;\n" +
               "            padding: 0;\n" +
               "            -webkit-tap-highlight-color: transparent;\n" +
               "        }\n" +
               "        \n" +
               "        body {\n" +
               "            font-family: 'Inter', -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;\n" +
               "            background: var(--bg-gradient);\n" +
               "            color: var(--text-primary);\n" +
               "            min-height: 100vh;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            align-items: center;\n" +
               "            justify-content: flex-start;\n" +
               "            padding: 20px 16px 40px 16px;\n" +
               "            line-height: 1.5;\n" +
               "        }\n" +
               "        \n" +
               "        .container {\n" +
               "            width: 100%;\n" +
               "            max-width: 500px;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            gap: 20px;\n" +
               "        }\n" +
               "        \n" +
               "        h1 {\n" +
               "            font-size: 2.2rem;\n" +
               "            font-weight: 800;\n" +
               "            text-align: center;\n" +
               "            letter-spacing: -0.025em;\n" +
               "            background: linear-gradient(to right, #60a5fa, #a5b4fc);\n" +
               "            -webkit-background-clip: text;\n" +
               "            -webkit-text-fill-color: transparent;\n" +
               "            margin-bottom: 5px;\n" +
               "        }\n" +
               "        \n" +
               "        h2 {\n" +
               "            font-size: 1.5rem;\n" +
               "            font-weight: 700;\n" +
               "            color: var(--text-primary);\n" +
               "            letter-spacing: -0.02em;\n" +
               "        }\n" +
               "        \n" +
               "        h3 {\n" +
               "            font-size: 1.2rem;\n" +
               "            font-weight: 600;\n" +
               "            color: var(--text-primary);\n" +
               "        }\n" +
               "        \n" +
               "        p, .desc {\n" +
               "            color: var(--text-muted);\n" +
               "            font-size: 0.95rem;\n" +
               "        }\n" +
               "        \n" +
               "        .card {\n" +
               "            background: var(--card-bg);\n" +
               "            backdrop-filter: blur(12px);\n" +
               "            -webkit-backdrop-filter: blur(12px);\n" +
               "            border: 1px solid var(--card-border);\n" +
               "            border-radius: 16px;\n" +
               "            padding: 20px;\n" +
               "            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4), 0 8px 10px -6px rgba(0, 0, 0, 0.4);\n" +
               "        }\n" +
               "        \n" +
               "        /* Reveal Card */\n" +
               "        .reveal-box {\n" +
               "            background: rgba(30, 41, 59, 0.4);\n" +
               "            border: 2px dashed rgba(255, 255, 255, 0.15);\n" +
               "            border-radius: 16px;\n" +
               "            padding: 35px 20px;\n" +
               "            text-align: center;\n" +
               "            cursor: pointer;\n" +
               "            position: relative;\n" +
               "            overflow: hidden;\n" +
               "            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);\n" +
               "            min-height: 120px;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            align-items: center;\n" +
               "            justify-content: center;\n" +
               "        }\n" +
               "        .reveal-box.blurred .reveal-content {\n" +
               "            filter: blur(24px);\n" +
               "            opacity: 0.05;\n" +
               "            pointer-events: none;\n" +
               "            user-select: none;\n" +
               "            transform: scale(0.95);\n" +
               "        }\n" +
               "        .reveal-box.blurred .reveal-placeholder {\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            align-items: center;\n" +
               "            gap: 8px;\n" +
               "        }\n" +
               "        .reveal-box.blurred .reveal-placeholder::before {\n" +
               "            content: '👁️';\n" +
               "            font-size: 1.8rem;\n" +
               "        }\n" +
               "        .reveal-box:not(.blurred) {\n" +
               "            border-style: solid;\n" +
               "            border-color: rgba(59, 130, 246, 0.4);\n" +
               "            background: rgba(59, 130, 246, 0.08);\n" +
               "        }\n" +
               "        .reveal-box:not(.blurred) .reveal-content {\n" +
               "            filter: none;\n" +
               "            opacity: 1;\n" +
               "            transform: scale(1);\n" +
               "        }\n" +
               "        .reveal-box:not(.blurred) .reveal-placeholder {\n" +
               "            display: none;\n" +
               "        }\n" +
               "        .reveal-content {\n" +
               "            transition: all 0.3s ease;\n" +
               "            width: 100%;\n" +
               "        }\n" +
               "        .reveal-placeholder {\n" +
               "            font-size: 1.1rem;\n" +
               "            font-weight: 600;\n" +
               "            color: var(--text-muted);\n" +
               "            transition: all 0.3s ease;\n" +
               "        }\n" +
               "        \n" +
               "        /* Interactive Elements */\n" +
               "        .btn {\n" +
               "            display: inline-flex;\n" +
               "            align-items: center;\n" +
               "            justify-content: center;\n" +
               "            padding: 14px 24px;\n" +
               "            font-size: 1rem;\n" +
               "            font-weight: 600;\n" +
               "            border-radius: 12px;\n" +
               "            border: none;\n" +
               "            cursor: pointer;\n" +
               "            transition: all 0.2s ease;\n" +
               "            color: #ffffff;\n" +
               "            width: 100%;\n" +
               "            text-decoration: none;\n" +
               "            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.2);\n" +
               "            text-align: center;\n" +
               "        }\n" +
               "        \n" +
               "        .btn-primary {\n" +
               "            background: linear-gradient(135deg, var(--primary), var(--primary-hover));\n" +
               "        }\n" +
               "        .btn-primary:active {\n" +
               "            transform: scale(0.98);\n" +
               "        }\n" +
               "        \n" +
               "        .btn-danger {\n" +
               "            background: linear-gradient(135deg, var(--danger), var(--danger-hover));\n" +
               "        }\n" +
               "        .btn-danger:active {\n" +
               "            transform: scale(0.98);\n" +
               "        }\n" +
               "        \n" +
               "        .btn-success {\n" +
               "            background: linear-gradient(135deg, var(--success), var(--success-hover));\n" +
               "        }\n" +
               "        .btn-success:active {\n" +
               "            transform: scale(0.98);\n" +
               "        }\n" +
               "        \n" +
               "        .input-text {\n" +
               "            width: 100%;\n" +
               "            padding: 14px 16px;\n" +
               "            background: rgba(15, 23, 42, 0.6);\n" +
               "            border: 1px solid var(--card-border);\n" +
               "            border-radius: 12px;\n" +
               "            color: var(--text-primary);\n" +
               "            font-size: 1.05rem;\n" +
               "            font-family: inherit;\n" +
               "            outline: none;\n" +
               "            transition: border-color 0.2s;\n" +
               "        }\n" +
               "        .input-text:focus {\n" +
               "            border-color: var(--primary);\n" +
               "            box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);\n" +
               "        }\n" +
               "        \n" +
               "        /* Grid/Lists */\n" +
               "        .item-list {\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            gap: 10px;\n" +
               "            width: 100%;\n" +
               "        }\n" +
               "        \n" +
               "        .item-row {\n" +
               "            display: flex;\n" +
               "            justify-content: space-between;\n" +
               "            align-items: center;\n" +
               "            padding: 12px 16px;\n" +
               "            background: rgba(30, 41, 59, 0.4);\n" +
               "            border: 1px solid var(--card-border);\n" +
               "            border-radius: 12px;\n" +
               "        }\n" +
               "        \n" +
               "        .badge {\n" +
               "            font-size: 0.75rem;\n" +
               "            padding: 4px 8px;\n" +
               "            border-radius: 9999px;\n" +
               "            font-weight: 600;\n" +
               "        }\n" +
               "        .badge-red { background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }\n" +
               "        .badge-green { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }\n" +
               "        .badge-orange { background: rgba(245, 158, 11, 0.15); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.3); }\n" +
               "        \n" +
               "        .qr-container {\n" +
               "            display: flex;\n" +
               "            justify-content: center;\n" +
               "            align-items: center;\n" +
               "            margin: 10px 0;\n" +
               "        }\n" +
               "        .qr-image {\n" +
               "            width: 220px;\n" +
               "            height: 220px;\n" +
               "            border-radius: 16px;\n" +
               "            border: 4px solid var(--card-bg);\n" +
               "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);\n" +
               "        }\n" +
               "        \n" +
               "        .header-section {\n" +
               "            text-align: center;\n" +
               "            margin-bottom: 10px;\n" +
               "        }\n" +
               "    </style>\n" +
               "    " + additionalHead + "\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        " + content + "\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
}
