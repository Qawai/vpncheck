package com.qawai.vpncheck;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String[] IP_SRC = {
            "2ip.ru", "ipify", "ip.sb", "icanhazip", "ifconfig.me", "ipinfo.io"
    };
    private static final String[] IP_URL = {
            "https://2ip.ru/ip/",
            "https://api.ipify.org",
            "https://api.ip.sb/ip",
            "https://ipv4.icanhazip.com",
            "https://ifconfig.me/ip",
            "https://ipinfo.io/ip"
    };

    // имя, хост, категория
    private static final String[][] SERVICES = {
            {"Discord", "discord.com", "Соцсети и мессенджеры"},
            {"Discord CDN", "cdn.discordapp.com", "Соцсети и мессенджеры"},
            {"Telegram", "api.telegram.org", "Соцсети и мессенджеры"},
            {"VK", "vk.com", "Соцсети и мессенджеры"},
            {"Mail.ru", "mail.ru", "Соцсети и мессенджеры"},
            {"X (Twitter)", "x.com", "Соцсети и мессенджеры"},
            {"Instagram", "www.instagram.com", "Соцсети и мессенджеры"},
            {"Facebook", "www.facebook.com", "Соцсети и мессенджеры"},
            {"TikTok", "www.tiktok.com", "Соцсети и мессенджеры"},
            {"Reddit", "www.reddit.com", "Соцсети и мессенджеры"},

            {"YouTube", "youtube.com", "Стриминг и видео"},
            {"Twitch", "www.twitch.tv", "Стриминг и видео"},
            {"Netflix", "www.netflix.com", "Стриминг и видео"},
            {"Spotify", "open.spotify.com", "Стриминг и видео"},

            {"Google", "google.com", "Поиск и сервисы"},
            {"Yandex", "ya.ru", "Поиск и сервисы"},
            {"Yandex Disk", "disk.yandex.ru", "Поиск и сервисы"},
            {"Wikipedia", "www.wikipedia.org", "Поиск и сервисы"},
            {"OpenAI", "api.openai.com", "Поиск и сервисы"},
            {"Zoom", "zoom.us", "Поиск и сервисы"},

            {"Wildberries", "www.wildberries.ru", "Магазины"},
            {"Ozon", "www.ozon.ru", "Магазины"},

            {"Steam", "store.steampowered.com", "Игры"},
            {"Steam CDN", "cdn.cloudflare.steamstatic.com", "Игры"},
            {"Roblox", "www.roblox.com", "Игры"},

            {"Cloudflare", "1.1.1.1", "Сеть"},
            {"Cloudflare DNS", "one.one.one.one", "Сеть"}
    };

    private static final String[] CATEGORIES = {
            "Соцсети и мессенджеры", "Стриминг и видео", "Поиск и сервисы",
            "Магазины", "Игры", "Сеть", "Иные"
    };

    private static final Map<String, String> HOST_NAME =
            new LinkedHashMap<String, String>();
    private static final Map<String, String> HOST_CAT =
            new LinkedHashMap<String, String>();
    static {
        for (String[] s : SERVICES) {
            HOST_NAME.put(s[1], s[0]);
            HOST_CAT.put(s[1], s[2]);
        }
    }

    private static final Map<String, String> COUNTRY_RU =
            new LinkedHashMap<String, String>();
    static {
        COUNTRY_RU.put("RU", "Россия");
        COUNTRY_RU.put("UA", "Украина");
        COUNTRY_RU.put("BY", "Беларусь");
        COUNTRY_RU.put("KZ", "Казахстан");
        COUNTRY_RU.put("AM", "Армения");
        COUNTRY_RU.put("GE", "Грузия");
        COUNTRY_RU.put("AZ", "Азербайджан");
        COUNTRY_RU.put("UZ", "Узбекистан");
        COUNTRY_RU.put("KG", "Киргизия");
        COUNTRY_RU.put("TJ", "Таджикистан");
        COUNTRY_RU.put("TM", "Туркменистан");
        COUNTRY_RU.put("MD", "Молдова");
        COUNTRY_RU.put("US", "США");
        COUNTRY_RU.put("DE", "Германия");
        COUNTRY_RU.put("NL", "Нидерланды");
        COUNTRY_RU.put("FI", "Финляндия");
        COUNTRY_RU.put("PL", "Польша");
        COUNTRY_RU.put("TR", "Турция");
        COUNTRY_RU.put("GB", "Великобритания");
        COUNTRY_RU.put("FR", "Франция");
        COUNTRY_RU.put("JP", "Япония");
        COUNTRY_RU.put("CN", "Китай");
        COUNTRY_RU.put("KR", "Республика Корея");
        COUNTRY_RU.put("SE", "Швеция");
        COUNTRY_RU.put("NO", "Норвегия");
        COUNTRY_RU.put("DK", "Дания");
        COUNTRY_RU.put("IS", "Исландия");
        COUNTRY_RU.put("CH", "Швейцария");
        COUNTRY_RU.put("AT", "Австрия");
        COUNTRY_RU.put("BE", "Бельгия");
        COUNTRY_RU.put("IT", "Италия");
        COUNTRY_RU.put("ES", "Испания");
        COUNTRY_RU.put("PT", "Португалия");
        COUNTRY_RU.put("CZ", "Чехия");
        COUNTRY_RU.put("SK", "Словакия");
        COUNTRY_RU.put("RO", "Румыния");
        COUNTRY_RU.put("BG", "Болгария");
        COUNTRY_RU.put("RS", "Сербия");
        COUNTRY_RU.put("GR", "Греция");
        COUNTRY_RU.put("HU", "Венгрия");
        COUNTRY_RU.put("LT", "Литва");
        COUNTRY_RU.put("LV", "Латвия");
        COUNTRY_RU.put("EE", "Эстония");
        COUNTRY_RU.put("IE", "Ирландия");
        COUNTRY_RU.put("CA", "Канада");
        COUNTRY_RU.put("AU", "Австралия");
        COUNTRY_RU.put("BR", "Бразилия");
        COUNTRY_RU.put("AR", "Аргентина");
        COUNTRY_RU.put("MX", "Мексика");
        COUNTRY_RU.put("IN", "Индия");
        COUNTRY_RU.put("SG", "Сингапур");
        COUNTRY_RU.put("HK", "Гонконг");
        COUNTRY_RU.put("IL", "Израиль");
        COUNTRY_RU.put("AE", "ОАЭ");
        COUNTRY_RU.put("EG", "Египет");
        COUNTRY_RU.put("ZA", "ЮАР");
        COUNTRY_RU.put("ID", "Индонезия");
        COUNTRY_RU.put("MY", "Малайзия");
        COUNTRY_RU.put("TH", "Таиланд");
        COUNTRY_RU.put("VN", "Вьетнам");
        COUNTRY_RU.put("PH", "Филиппины");
        COUNTRY_RU.put("CY", "Кипр");
        COUNTRY_RU.put("LU", "Люксембург");
        COUNTRY_RU.put("MT", "Мальта");
    }

    private static final Pattern IP_RE =
            Pattern.compile("\\b(\\d{1,3}\\.){3}\\d{1,3}\\b");

    private static final String[] THEME_NAMES = {
            "Тёмная", "Чёрная (AMOLED)", "Светлая", "Терминал",
            "Мягкий светлый", "Мягкий тёмный", "Лаванда", "Мята", "Системная"
    };
    private static final boolean[] IS_LIGHT = {
            false, false, true, false, true, false, false, false, false
    };
    // bg, text, accent, grey, good, warn, bad
    private static final int[][] THEMES = {
            {0xFF101418, 0xFFE8EAED, 0xFF4FC3F7, 0xFF9AA0A6,
             0xFF66BB6A, 0xFFFFD54F, 0xFFEF5350},
            {0xFF000000, 0xFFE8EAED, 0xFF00E676, 0xFF757575,
             0xFF00E676, 0xFFFFD54F, 0xFFEF5350},
            {0xFFF5F5F5, 0xFF212121, 0xFF1565C0, 0xFF757575,
             0xFF2E7D32, 0xFFF9A825, 0xFFC62828},
            {0xFF0D1117, 0xFF3FB950, 0xFF3FB950, 0xFF8B949E,
             0xFF3FB950, 0xFFD29922, 0xFFF85149},
            {0xFFFAF7F2, 0xFF4A4440, 0xFF9C7A54, 0xFF9E958D,
             0xFF5B9A6E, 0xFFC9A227, 0xFFC46B6B},
            {0xFF22201E, 0xFFE8E2DB, 0xFFC9A97E, 0xFF9A928A,
             0xFF7CB98F, 0xFFD4B04A, 0xFFD08080},
            {0xFF232029, 0xFFE6E1F0, 0xFFB8A1D9, 0xFF9A93A8,
             0xFF8FC7A5, 0xFFD9C06B, 0xFFD98A9A},
            {0xFF1B2422, 0xFFDDEAE5, 0xFF8FD6BD, 0xFF8FA39C,
             0xFF8FD6BD, 0xFFD6C56F, 0xFFE08A8A}
    };
    // оранжевый для "медленных, но рабочих"
    private static final int[] ORANGE = {
            0xFFFFA726, 0xFFFFA726, 0xFFEF6C00, 0xFFD99A2B,
            0xFFC98A2E, 0xFFD69A4A, 0xFFC79A5A, 0xFF4FB0A0
    };

    private static final String NOTIF_CHANNEL = "status";
    private static final String PINNED_KEY = "★ Закреплённые";

    // эндпоинты для замера скорости скачивания
    private static final String[] SPEED_URLS = {
            "https://speed.cloudflare.com/__down?bytes=10000000",
            "http://speedtest.tele2.net/10MB.zip",
            "https://speed.cloudflare.com/__down?bytes=5000000",
            "http://ftp.debian.org/debian/README"
    };
    private static final String SPEED_UPLOAD_URL = "https://speed.cloudflare.com/__up";

    // пороги времени отклика: до 500 мс — зелёные, до 1800 — жёлтые, дальше красные
    private static final int GOOD_MS = 500;
    private static final int WARN_MS = 1800;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Runnable autoRefresh = new Runnable() {
        @Override public void run() {
            refresh();
            ui.postDelayed(this, 45000);
        }
    };

    private TextView ipView, deepIpView, countryView, infoView, themeBtn, notifyBtn;
    private ProgressBar progress;
    private TextView btn, speedBtn;
    private LinearLayout listRoot, pinnedContainer, availabilityContainer, speedContainer;
    private TextView tabAvail, tabSpeed, speedResultView, speedServerView;
    private final Map<String, LinearLayout> catContainers =
            new LinkedHashMap<String, LinearLayout>();
    private final Map<String, TextView> valueViews =
            new ConcurrentHashMap<String, TextView>();
    private final Map<String, String> resultTexts =
            new ConcurrentHashMap<String, String>();
    private final Map<String, Integer> resultMs =
            new ConcurrentHashMap<String, Integer>();
    private final Map<String, Integer> resultCodes =
            new ConcurrentHashMap<String, Integer>();

    // пользовательские сервисы, переименования, удалённые системные
    private final List<String[]> customServices = new ArrayList<String[]>();
    private final Map<String, String> renames = new LinkedHashMap<String, String>();
    private final Set<String> deleted = new HashSet<String>();
    private final Set<String> collapsed = new HashSet<String>();
    private boolean editMode;
    private TextView summaryView, manageBtn, addBtn, shareBtn;
    private GaugeView gaugeView;
    private TextView upView, jitView, lossView;

    private EditText searchView;
    private String query = "";
    private boolean notifOn;
    private int lastEffectiveTheme = -1;
    private final BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!busy) refresh();
        }
    };

    private Set<String> stars = new HashSet<String>();
    private boolean busy;
    private float dp;
    private int themeIdx;
    private SharedPreferences prefs;

    private String dragHost;
    private View dragRow;
    private float startRawY;
    private PopupWindow popup;

    private int bg, text, accent, grey, good, warn, bad, orange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dp = getResources().getDisplayMetrics().density;
        prefs = getSharedPreferences("vpncheck", MODE_PRIVATE);
        notifOn = prefs.getBoolean("notif", false);
        themeIdx = prefs.getInt("theme", 0);
        stars = new HashSet<String>(prefs.getStringSet("stars", new HashSet<String>()));
        loadCustom();
        applyTheme();
        lastEffectiveTheme = effectiveTheme();
        applyThemeIcon();
        buildUi();
        renderLists();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // системная тема: переключаем иконку/оформление при смене тёмного режима
        if (themeIdx == THEMES.length) {
            int eff = effectiveTheme();
            if (eff != lastEffectiveTheme) {
                lastEffectiveTheme = eff;
                recreate();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            registerReceiver(netReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception ignored) {}
        refresh();
        ui.removeCallbacks(autoRefresh);
        ui.postDelayed(autoRefresh, 45000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ui.removeCallbacks(autoRefresh);
        try { unregisterReceiver(netReceiver); } catch (Exception ignored) {}
    }

    private void applyTheme() {
        int[] t = THEMES[effectiveTheme()];
        bg = t[0]; text = t[1]; accent = t[2]; grey = t[3];
        good = t[4]; warn = t[5]; bad = t[6];
        orange = ORANGE[effectiveTheme()];
    }

    private boolean isNight() {
        int mode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    // для "Системной" темы берём Тёмную или Светлую в зависимости от режима
    private int effectiveTheme() {
        if (themeIdx == THEMES.length) return isNight() ? 0 : 2;
        return themeIdx;
    }

    // иконка приложения меняется под выбранную тему (переключаем activity-alias)
    private void applyThemeIcon() {
        try {
            PackageManager pm = getPackageManager();
            String pkg = getPackageName();
            int idx = effectiveTheme();
            // сначала включаем нужную, потом выключаем остальные,
            // чтобы иконка в лаунчере не "пропадала" на мгновение
            pm.setComponentEnabledSetting(
                    new ComponentName(pkg, pkg + ".MainActivityTheme" + idx),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            for (int i = 0; i < THEMES.length; i++) {
                if (i == idx) continue;
                pm.setComponentEnabledSetting(
                        new ComponentName(pkg, pkg + ".MainActivityTheme" + i),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        } catch (Exception e) {
            // часть оболочек не поддерживает смену иконки на лету
        }
    }

    private int d(int v) { return Math.round(v * dp); }

    private TextView label(String s, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        return t;
    }

    // ободок вокруг кнопок, чтобы отличались от обычного текста
    private void styleButton(View v) {
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(d(2), accent);
        gd.setCornerRadius(d(10));
        gd.setColor(IS_LIGHT[themeIdx] ? 0x0F000000 : 0x14FFFFFF);
        v.setBackground(gd);
    }

    // параметры ячейки кнопки: равная ширина в строке
    private LinearLayout.LayoutParams cellParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(d(4), d(4), d(4), d(4));
        return lp;
    }

    // залитая акцентным цветом кнопка (для главного действия)
    private GradientDrawable solidAccent() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(accent);
        gd.setCornerRadius(d(12));
        return gd;
    }

    private void buildUi() {
        final ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(d(16), d(16), d(16), d(24));
        scroll.addView(root);

        LinearLayout head = new LinearLayout(this);
        head.addView(label("Check Connection", 24, accent, true));
        head.addView(label("  v2.0.3", 12, grey, false));
        root.addView(head);

        ipView = label("IP: ...", 18, text, true);
        ipView.setTypeface(Typeface.MONOSPACE);
        ipView.setPadding(0, d(14), 0, 0);
        ipView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String t = ipView.getText().toString().replace("IP: ", "");
                if (t.length() > 0) {
                    ClipboardManager cm =
                            (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("IP", t));
                    Toast.makeText(MainActivity.this, "IP скопирован",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        root.addView(ipView);

        deepIpView = label("", 14, grey, false);
        deepIpView.setTypeface(Typeface.MONOSPACE);
        deepIpView.setPadding(0, d(4), 0, 0);
        deepIpView.setVisibility(View.GONE);
        root.addView(deepIpView);

        countryView = label("", 16, accent, true);
        countryView.setPadding(0, d(6), 0, 0);
        countryView.setVisibility(View.GONE);
        root.addView(countryView);

        infoView = label("", 14, grey, false);
        infoView.setPadding(0, d(2), 0, 0);
        root.addView(infoView);

        LinearLayout row1 = new LinearLayout(this);
        row1.setPadding(0, d(14), 0, 0);

        btn = new TextView(this);
        btn.setText("Обновить");
        btn.setTextSize(15);
        btn.setTextColor(accent);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(d(10), d(12), d(10), d(12));
        styleButton(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { refresh(); }
        });
        row1.addView(btn, cellParams());

        themeBtn = new TextView(this);
        themeBtn.setText(THEME_NAMES[themeIdx] + "  ▾");
        themeBtn.setTextColor(accent);
        themeBtn.setTextSize(15);
        themeBtn.setGravity(Gravity.CENTER);
        themeBtn.setPadding(d(10), d(12), d(10), d(12));
        styleButton(themeBtn);
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showThemeMenu(v); }
        });
        row1.addView(themeBtn, cellParams());

        notifyBtn = new TextView(this);
        notifyBtn.setText(notifOn ? "Уведомления ✓" : "Уведомления");
        notifyBtn.setTextColor(notifOn ? accent : grey);
        notifyBtn.setTextSize(14);
        notifyBtn.setGravity(Gravity.CENTER);
        notifyBtn.setPadding(d(10), d(12), d(10), d(12));
        styleButton(notifyBtn);
        notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleNotifications(); }
        });
        row1.addView(notifyBtn, cellParams());

        root.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setPadding(0, d(4), 0, 0);

        shareBtn = new TextView(this);
        shareBtn.setText("Поделиться");
        shareBtn.setTextColor(accent);
        shareBtn.setTextSize(14);
        shareBtn.setGravity(Gravity.CENTER);
        shareBtn.setPadding(d(10), d(12), d(10), d(12));
        styleButton(shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { shareResults(); }
        });
        row2.addView(shareBtn, cellParams());

        manageBtn = new TextView(this);
        manageBtn.setText(editMode ? "Готово" : "Управление");
        manageBtn.setTextColor(accent);
        manageBtn.setTextSize(14);
        manageBtn.setGravity(Gravity.CENTER);
        manageBtn.setPadding(d(10), d(12), d(10), d(12));
        styleButton(manageBtn);
        manageBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                editMode = !editMode;
                manageBtn.setText(editMode ? "Готово" : "Управление");
                renderLists();
            }
        });
        row2.addView(manageBtn, cellParams());

        addBtn = new TextView(this);
        addBtn.setText("+ Добавить сайт");
        addBtn.setTextColor(accent);
        addBtn.setTextSize(14);
        addBtn.setGravity(Gravity.CENTER);
        addBtn.setPadding(d(10), d(12), d(10), d(12));
        styleButton(addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAddDialog(); }
        });
        row2.addView(addBtn, cellParams());

        root.addView(row2);

        progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams pp =
                new LinearLayout.LayoutParams(-2, -2);
        pp.topMargin = d(4);
        root.addView(progress, pp);

        // вкладки
        LinearLayout tabs = new LinearLayout(this);
        tabs.setPadding(0, d(10), 0, 0);
        tabAvail = new TextView(this);
        tabAvail.setText("Доступность");
        tabAvail.setTextSize(16);
        tabAvail.setPadding(d(18), d(10), d(18), d(10));
        styleButton(tabAvail);
        tabAvail.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(true); }
        });
        tabs.addView(tabAvail);
        tabSpeed = new TextView(this);
        tabSpeed.setText("Скорость");
        tabSpeed.setTextSize(16);
        tabSpeed.setPadding(d(18), d(10), d(18), d(10));
        styleButton(tabSpeed);
        tabSpeed.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(false); }
        });
        tabs.addView(tabSpeed);
        root.addView(tabs);

        summaryView = label("", 14, accent, true);
        summaryView.setPadding(0, d(10), 0, 0);
        root.addView(summaryView);

        // контейнер доступности
        availabilityContainer = new LinearLayout(this);
        availabilityContainer.setOrientation(LinearLayout.VERTICAL);

        searchView = new EditText(this);
        searchView.setHint("Поиск сервиса…");
        searchView.setTextColor(text);
        searchView.setHintTextColor(grey);
        searchView.setSingleLine(true);
        searchView.setBackground(null);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase();
                renderLists();
            }
        });
        availabilityContainer.addView(searchView);

        availabilityContainer.addView(label("тап — закрепить ★ · удержание — перетащить",
                12, grey, false));

        listRoot = new LinearLayout(this);
        listRoot.setOrientation(LinearLayout.VERTICAL);
        listRoot.setPadding(0, d(6), 0, 0);
        availabilityContainer.addView(listRoot);
        root.addView(availabilityContainer);

        // контейнер замера скорости
        speedContainer = new LinearLayout(this);
        speedContainer.setOrientation(LinearLayout.VERTICAL);
        speedContainer.setVisibility(View.GONE);

        speedBtn = new TextView(this);
        speedBtn.setText("Замерить скорость");
        speedBtn.setTextSize(16);
        speedBtn.setTextColor(0xFFFFFFFF);
        speedBtn.setGravity(Gravity.CENTER);
        speedBtn.setPadding(d(14), d(16), d(14), d(16));
        speedBtn.setBackground(solidAccent());
        speedBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { runSpeedTest(); }
        });
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sp.topMargin = d(8);
        speedContainer.addView(speedBtn, sp);

        gaugeView = new GaugeView(this);
        gaugeView.setTheme(accent, good, warn);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d(130));
        gp.gravity = Gravity.CENTER_HORIZONTAL;
        gp.topMargin = d(6);
        speedContainer.addView(gaugeView, gp);

        speedResultView = label("—", 22, text, true);
        speedResultView.setGravity(Gravity.CENTER);
        speedResultView.setPadding(0, d(8), 0, 0);
        speedContainer.addView(speedResultView);

        upView = label("", 15, text, false);
        upView.setGravity(Gravity.CENTER);
        upView.setPadding(0, d(2), 0, 0);
        speedContainer.addView(upView);

        jitView = label("", 14, grey, false);
        jitView.setGravity(Gravity.CENTER);
        jitView.setPadding(0, d(2), 0, 0);
        speedContainer.addView(jitView);

        lossView = label("", 14, grey, false);
        lossView.setGravity(Gravity.CENTER);
        lossView.setPadding(0, d(2), 0, 0);
        speedContainer.addView(lossView);

        speedServerView = label("", 13, grey, false);
        speedServerView.setGravity(Gravity.CENTER);
        speedServerView.setPadding(0, d(4), 0, 0);
        speedContainer.addView(speedServerView);

        root.addView(speedContainer);

        pinnedContainer = new LinearLayout(this);
        pinnedContainer.setOrientation(LinearLayout.VERTICAL);

        for (String cat : CATEGORIES) {
            catContainers.put(cat, new LinearLayout(this));
            catContainers.get(cat).setOrientation(LinearLayout.VERTICAL);
        }

        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (dragRow != null) scroll.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        setContentView(scroll);
        showTab(true);
    }

    private void showTab(boolean availability) {
        availabilityContainer.setVisibility(availability ? View.VISIBLE : View.GONE);
        speedContainer.setVisibility(availability ? View.GONE : View.VISIBLE);
        tabAvail.setTextColor(availability ? accent : grey);
        tabSpeed.setTextColor(availability ? grey : accent);
    }

    private void showThemeMenu(View anchor) {
        boolean light = IS_LIGHT[themeIdx];
        int surface = light ? 0xFFFFFFFF : 0xFF2B2B2B;
        int menuText = light ? 0xFF212121 : 0xFFE8EAED;

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bgDraw = new GradientDrawable();
        bgDraw.setColor(surface);
        bgDraw.setCornerRadius(d(10));
        menu.setBackground(bgDraw);
        menu.setPadding(0, d(6), 0, d(6));
        menu.setMinimumWidth(d(210));

        for (int i = 0; i < THEME_NAMES.length; i++) {
            final int idx = i;
            TextView item = new TextView(this);
            item.setText((i == themeIdx ? "●  " : "     ") + THEME_NAMES[i]);
            item.setTextSize(15);
            item.setTextColor(i == themeIdx ? accent : menuText);
            item.setPadding(d(20), d(12), d(20), d(12));
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    themeIdx = idx;
                    prefs.edit().putInt("theme", idx).apply();
                    applyThemeIcon();
                    if (popup != null) popup.dismiss();
                    recreate();
                }
            });
            menu.addView(item);
        }

        popup = new PopupWindow(menu,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popup.showAsDropDown(anchor);
    }

    private void renderLists() {
        listRoot.removeAllViews();
        valueViews.clear();

        List<String> pinned = loadOrder("pinned");
        List<String> visPinned = new ArrayList<String>();
        for (String h : pinned) if (stars.contains(h) && exists(h) && matches(h))
            visPinned.add(h);
        for (String h : allHosts()) if (stars.contains(h) && matches(h)
                && !visPinned.contains(h)) visPinned.add(h);

        if (!visPinned.isEmpty()) {
            TextView pinHeader = sectionHeader(
                    (collapsed.contains(PINNED_KEY) ? "▸ " : "▾ ") + "★ Закреплённые");
            pinHeader.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { toggleCollapse(PINNED_KEY); }
            });
            listRoot.addView(pinHeader);
            pinnedContainer.removeAllViews();
            if (!collapsed.contains(PINNED_KEY))
                for (String h : visPinned) pinnedContainer.addView(createRow(h));
            listRoot.addView(pinnedContainer);
        }

        for (String cat : CATEGORIES) {
            LinearLayout cont = catContainers.get(cat);
            cont.removeAllViews();
            for (String h : loadOrder("cat_" + cat)) {
                if (stars.contains(h) || !inCategory(h, cat) || !matches(h)) continue;
                cont.addView(createRow(h));
            }
            if (cont.getChildCount() == 0) continue;
            final String c = cat;
            TextView header = sectionHeader(
                    (collapsed.contains(cat) ? "▸ " : "▾ ") + cat);
            header.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { toggleCollapse(c); }
            });
            listRoot.addView(header);
            cont.setVisibility(collapsed.contains(cat) ? View.GONE : View.VISIBLE);
            listRoot.addView(cont);
        }
    }

    private void toggleCollapse(String key) {
        if (collapsed.contains(key)) collapsed.remove(key);
        else collapsed.add(key);
        saveCollapsed();
        renderLists();
    }

    private boolean matches(String host) {
        if (query == null || query.isEmpty()) return true;
        return nameOf(host).toLowerCase().contains(query)
                || host.toLowerCase().contains(query);
    }

    private TextView sectionHeader(String title) {
        TextView h = label(title, 15, accent, true);
        h.setPadding(0, d(14), 0, d(4));
        return h;
    }

    private void resetDrag() {
        if (dragRow != null) {
            try {
                dragRow.setTranslationY(0);
                dragRow.setAlpha(1f);
                dragRow.setBackgroundColor(0);
            } catch (Throwable ignored) {}
        }
        dragHost = null;
        dragRow = null;
    }

    private LinearLayout createRow(final String host) {
        LinearLayout row = new LinearLayout(this);
        row.setPadding(0, d(4), d(4), d(4));
        row.setTag(host);
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (editMode || dragHost != null) return;
                if (stars.contains(host)) stars.remove(host);
                else stars.add(host);
                saveStars();
                renderLists();
            }
        });
        row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (editMode) return false;
                dragHost = host;
                dragRow = v;
                v.setAlpha(0.85f);
                v.setBackgroundColor(0x33FFFFFF);
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
        });
        row.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (!host.equals(dragHost)) return false;
                try {
                    switch (e.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            startRawY = e.getRawY();
                            return false;
                        case MotionEvent.ACTION_MOVE: {
                            if (dragRow == null) return true;
                            float dy = e.getRawY() - startRawY;
                            LinearLayout parent = (LinearLayout) dragRow.getParent();
                            if (parent == null) { resetDrag(); return true; }
                            dragRow.setTranslationY(dy);
                            int idx = parent.indexOfChild(dragRow);
                            if (idx < 0) { resetDrag(); return true; }
                            parent.requestDisallowInterceptTouchEvent(true);
                            if (idx < parent.getChildCount() - 1) {
                                View next = parent.getChildAt(idx + 1);
                                if (dragRow.getTop() + dy
                                        > next.getTop() + next.getHeight() / 2f) {
                                    startRawY += next.getHeight();
                                    parent.removeViewAt(idx);
                                    parent.addView(dragRow, idx + 1);
                                    dragRow.setTranslationY(e.getRawY() - startRawY);
                                    return true;
                                }
                            }
                            if (idx > 0) {
                                View prev = parent.getChildAt(idx - 1);
                                if (dragRow.getTop() + dy
                                        < prev.getTop() + prev.getHeight() / 2f) {
                                    startRawY -= prev.getHeight();
                                    parent.removeViewAt(idx);
                                    parent.addView(dragRow, idx - 1);
                                    dragRow.setTranslationY(e.getRawY() - startRawY);
                                }
                            }
                            return true;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (dragRow != null) {
                                dragRow.setTranslationY(0);
                                dragRow.setAlpha(1f);
                                dragRow.setBackgroundColor(0);
                            }
                            try { saveOrder(); } catch (Throwable ignored) {}
                            dragHost = null;
                            dragRow = null;
                            return true;
                    }
                } catch (Throwable t) {
                    // любая ошибка драга не должна ронять приложение
                    resetDrag();
                }
                return false;
            }
        });

        TextView star = label(stars.contains(host) ? "★" : "☆", 15,
                stars.contains(host) ? accent : grey, false);
        star.setMinWidth(d(26));
        row.addView(star);

        TextView name = label(nameOf(host), 13, text, false);
        name.setMinWidth(d(120));
        row.addView(name);

        TextView val = label("…", 13, grey, false);
        val.setTypeface(Typeface.MONOSPACE);
        String prev = resultTexts.get(host);
        if (prev != null) {
            val.setText(prev);
            val.setTextColor(colorFor(host));
        }
        row.addView(val);
        valueViews.put(host, val);

        if (editMode) {
            TextView ren = label("✎", 16, accent, false);
            ren.setPadding(d(10), 0, d(10), 0);
            ren.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showRenameDialog(host); }
            });
            row.addView(ren);

            TextView del = label("✕", 16, bad, false);
            del.setPadding(d(10), 0, 0, 0);
            del.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showDeleteDialog(host); }
            });
            row.addView(del);
        }
        return row;
    }

    private int colorFor(String host) {
        Integer code = resultCodes.get(host);
        if (code == null || code <= 0) return bad;      // недоступен
        Integer ms = resultMs.get(host);
        if (ms == null) return grey;
        if (ms <= GOOD_MS) return good;                 // зелёный
        if (ms <= WARN_MS) return warn;                 // жёлтый
        return orange;                                  // оранжевый
    }

    private static String trendArrow(int t) {
        return t > 0 ? " ↑" : t < 0 ? " ↓" : " =";
    }

    private boolean isCustom(String host) {
        for (String[] s : customServices) if (s[1].equals(host)) return true;
        return false;
    }

    private boolean exists(String host) {
        return HOST_NAME.containsKey(host) || isCustom(host);
    }

    private String nameOf(String host) {
        String r = renames.get(host);
        if (r != null) return r;
        String n = HOST_NAME.get(host);
        if (n != null) return n;
        for (String[] s : customServices) if (s[1].equals(host)) return s[0];
        return host;
    }

    private boolean inCategory(String host, String cat) {
        if (isCustom(host)) return cat.equals("Иные");
        return cat.equals(HOST_CAT.get(host));
    }

    private List<String> allHosts() {
        List<String> l = new ArrayList<String>();
        for (String[] s : SERVICES) {
            if (!deleted.contains(s[1])) l.add(s[1]);
        }
        for (String[] s : customServices) l.add(s[1]);
        return l;
    }

    private List<String> loadOrder(String key) {
        String saved = prefs.getString("order_" + key, "");
        List<String> out = new ArrayList<String>();
        if (saved.length() > 0) {
            for (String h : saved.split(",")) {
                if (h.length() > 0 && exists(h)
                        && (key.equals("pinned") ? !out.contains(h) : true)) out.add(h);
            }
        }
        if (key.equals("pinned")) return out;
        String cat = key.substring(4);
        for (String[] s : SERVICES)
            if (s[2].equals(cat) && !out.contains(s[1])) out.add(s[1]);
        if (cat.equals("Иные"))
            for (String[] s : customServices)
                if (!out.contains(s[1])) out.add(s[1]);
        return out;
    }

    private void saveOrder() {
        SharedPreferences.Editor ed = prefs.edit();
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < pinnedContainer.getChildCount(); i++) {
            if (i > 0) pin.append(',');
            pin.append(pinnedContainer.getChildAt(i).getTag());
        }
        ed.putString("order_pinned", pin.toString());
        for (String cat : CATEGORIES) {
            LinearLayout cont = catContainers.get(cat);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cont.getChildCount(); i++) {
                if (i > 0) sb.append(',');
                sb.append(cont.getChildAt(i).getTag());
            }
            ed.putString("order_cat_" + cat, sb.toString());
        }
        ed.apply();
    }

    private void saveStars() {
        prefs.edit()
                .putStringSet("stars", new LinkedHashSet<String>(stars)).apply();
    }

    // разделители для хранения: record=\u001E, field=\u001F
    private void loadCustom() {
        customServices.clear();
        String cs = prefs.getString("custom", "");
        if (cs.length() > 0) {
            for (String item : cs.split("\u001E")) {
                String[] p = item.split("\u001F", 2);
                if (p.length == 2 && p[1].length() > 0)
                    customServices.add(new String[]{p[0], p[1]});
            }
        }
        renames.clear();
        String rn = prefs.getString("renames", "");
        if (rn.length() > 0) {
            for (String item : rn.split("\u001E")) {
                String[] p = item.split("\u001F", 2);
                if (p.length == 2 && p[1].length() > 0) renames.put(p[0], p[1]);
            }
        }
        deleted.clear();
        String dl = prefs.getString("deleted", "");
        if (dl.length() > 0) {
            for (String h : dl.split(",")) if (h.length() > 0) deleted.add(h);
        }
        collapsed.clear();
        String cl = prefs.getString("collapsed", "");
        if (cl.length() > 0) {
            for (String c : cl.split(",")) if (c.length() > 0) collapsed.add(c);
        }
    }

    private void saveCollapsed() {
        StringBuilder sb = new StringBuilder();
        for (String c : collapsed) { if (sb.length() > 0) sb.append(','); sb.append(c); }
        prefs.edit().putString("collapsed", sb.toString()).apply();
    }

    private void saveCustom() {
        StringBuilder cs = new StringBuilder();
        for (String[] s : customServices) {
            if (cs.length() > 0) cs.append('\u001E');
            cs.append(s[0]).append('\u001F').append(s[1]);
        }
        StringBuilder rn = new StringBuilder();
        for (Map.Entry<String, String> e : renames.entrySet()) {
            if (rn.length() > 0) rn.append('\u001E');
            rn.append(e.getKey()).append('\u001F').append(e.getValue());
        }
        StringBuilder dl = new StringBuilder();
        for (String h : deleted) {
            if (dl.length() > 0) dl.append(',');
            dl.append(h);
        }
        prefs.edit()
                .putString("custom", cs.toString())
                .putString("renames", rn.toString())
                .putString("deleted", dl.toString())
                .apply();
    }

    private void refresh() {
        if (busy) return;
        busy = true;
        btn.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        resultTexts.clear();
        resultMs.clear();
        resultCodes.clear();
        for (TextView v : valueViews.values()) {
            v.setText("…");
            v.setTextColor(grey);
        }

        pool.execute(new Runnable() {
            @Override public void run() {
                try {
                final List<String> hosts = allHosts();
                final Counter done = new Counter();
                for (final String host : hosts) {
                    pool.execute(new Runnable() {
                        @Override public void run() {
                            CheckResult cr = httpCheck(host);
                            synchronized (resultMs) {
                                if (cr != null) {
                                    int prev = prefs.getInt("hist_" + host, -1);
                                    resultMs.put(host, cr.ms);
                                    resultCodes.put(host, cr.code);
                                    int trend = prev < 0 ? 0
                                            : cr.ms > prev ? 1 : cr.ms < prev ? -1 : 0;
                                    prefs.edit().putInt("hist_" + host, cr.ms).apply();
                                    // у ответивших не показываем код — только время
                                    resultTexts.put(host,
                                            cr.ms + " мс" + trendArrow(trend));
                                } else {
                                    resultCodes.put(host, 0);
                                    resultTexts.put(host, "недоступен");
                                }
                            }
                            final int finished = done.next();
                            ui.post(new Runnable() {
                                @Override public void run() {
                                    TextView tv = valueViews.get(host);
                                    if (tv != null) {
                                        tv.setText(resultTexts.get(host));
                                        tv.setTextColor(colorFor(host));
                                    }
                                    if (finished == hosts.size()) {
                                        progress.setVisibility(View.GONE);
                                        btn.setEnabled(true);
                                        busy = false;
                                        updateSummary();
                                        updateWidget();
                                        updatePersistentNotification();
                                    }
                                }
                            });
                        }
                    });
                }
                // === БЫСТРЫЙ режим: 2ip.ru как основной источник ===
                final String[] QUICK_URL = {
                        "https://2ip.ru/ip/",
                        "https://api.ipify.org",
                        "https://ifconfig.me/ip"
                };
                final Object qlock = new Object();
                final String[] quickIp = {null};
                final int[] qdone = {0};
                for (int i = 0; i < QUICK_URL.length; i++) {
                    final int k = i;
                    pool.execute(new Runnable() {
                        @Override public void run() {
                            String r = fetch(QUICK_URL[k]);
                            if (r != null) {
                                if (QUICK_URL[k].contains("2ip.ru")
                                        && r.contains("поискового бота")) {
                                    r = null;
                                } else {
                                    Matcher m = IP_RE.matcher(r);
                                    if (m.find()) {
                                        synchronized (qlock) {
                                            if (quickIp[0] == null)
                                                quickIp[0] = m.group();
                                            qdone[0]++;
                                            qlock.notifyAll();
                                        }
                                        return;
                                    }
                                }
                            }
                            synchronized (qlock) { qdone[0]++; qlock.notifyAll(); }
                        }
                    });
                }
                synchronized (qlock) {
                    while (quickIp[0] == null && qdone[0] < QUICK_URL.length) {
                        try { qlock.wait(1500); } catch (InterruptedException e) { break; }
                    }
                }
                final String surfaceIp = quickIp[0];
                ui.post(new Runnable() {
                    @Override public void run() {
                        if (surfaceIp != null) {
                            ipView.setText("IP: " + surfaceIp);
                            ipView.setTextColor(text);
                        }
                    }
                });

                // === ГЛУБОКИЙ режим: консенсус всех источников + детали ===
                final Map<String, String> votes =
                        new LinkedHashMap<String, String>();
                final Object vlock = new Object();
                final int[] vdone = {0};
                for (int i = 0; i < IP_URL.length; i++) {
                    final int k = i;
                    pool.execute(new Runnable() {
                        @Override public void run() {
                            String r = fetch(IP_URL[k]);
                            String got = null;
                            if (r != null) {
                                if (IP_SRC[k].equals("2ip.ru")
                                        && r.contains("поискового бота")) {
                                    r = null;
                                } else {
                                    Matcher m = IP_RE.matcher(r);
                                    if (m.find()) got = m.group();
                                }
                            }
                            synchronized (vlock) {
                                if (got != null)
                                    votes.put(IP_SRC[k], got);
                                vdone[0]++;
                                vlock.notifyAll();
                            }
                        }
                    });
                }
                synchronized (vlock) {
                    while (vdone[0] < IP_URL.length) {
                        try { vlock.wait(800); } catch (InterruptedException e) { break; }
                    }
                }

                String consensus = null;
                int agree = 0;
                for (Map.Entry<String, String> e : votes.entrySet()) {
                    int n = 0;
                    for (Map.Entry<String, String> e2 : votes.entrySet())
                        if (e2.getValue().equals(e.getValue())) n++;
                    if (n > agree) { agree = n; consensus = e.getValue(); }
                }
                final String deepIp = consensus != null ? consensus
                        : (votes.isEmpty() ? null : votes.values().iterator().next());
                final Map<String, String> fVotes = new LinkedHashMap<String, String>(votes);
                final int fAgreeCount = agree;
                final boolean fAgree = agree >= 2;
                final String f2ip = votes.get("2ip.ru");

                String org = "", country = "", city = "";
                String js = fetch("https://ipinfo.io/json");
                if (js != null) {
                    org = jsonField(js, "org");
                    country = jsonField(js, "country");
                    city = jsonField(js, "city");
                }
                final String fOrg = org, fCountry = country, fCity = city;

                ui.post(new Runnable() {
                    @Override public void run() {
                        if (deepIp == null) {
                            deepIpView.setVisibility(View.GONE);
                            if (surfaceIp == null) {
                                ipView.setText("IP: не определён");
                                ipView.setTextColor(bad);
                                countryView.setVisibility(View.GONE);
                                infoView.setText("Нет интернета или сервисы недоступны");
                            }
                            return;
                        }
                        deepIpView.setText("Глубокий: " + deepIp);
                        deepIpView.setVisibility(View.VISIBLE);
                        prefs.edit()
                                .putString("deep_ip", deepIp)
                                .putString("deep_country", fCountry)
                                .apply();
                        if (fCountry.length() == 2) {
                            String name = COUNTRY_RU.containsKey(fCountry)
                                    ? COUNTRY_RU.get(fCountry) : fCountry;
                            String line = flagEmoji(fCountry) + "  " + name;
                            if (fCity.length() > 0) line += " · " + fCity;
                            countryView.setText(line);
                            countryView.setVisibility(View.VISIBLE);
                        } else {
                            countryView.setVisibility(View.GONE);
                        }
                        StringBuilder meta = new StringBuilder();
                        if (fOrg.length() > 0) meta.append(fOrg).append('\n');
                        if (f2ip != null && !f2ip.equals(deepIp)) {
                            meta.append("2ip.ru показывает: ").append(f2ip).append('\n');
                        }
                        if (fAgree) {
                            meta.append("подтверждён ").append(fAgreeCount)
                                    .append(" сервисами");
                        } else if (fVotes.size() > 1) {
                            meta.append("источники расходятся:\n");
                            for (Map.Entry<String, String> e : fVotes.entrySet())
                                meta.append("• ").append(e.getKey())
                                        .append(" → ").append(e.getValue())
                                        .append('\n');
                        }
                        infoView.setText(meta.toString());
                    }
                });

                } catch (Throwable t) {
                    ui.post(new Runnable() {
                        @Override public void run() {
                            progress.setVisibility(View.GONE);
                            btn.setEnabled(true);
                            busy = false;
                        }
                    });
                }
            }
        });
    }

    // ===== замер скорости =====
    private void runSpeedTest() {
        if (speedBtn == null) return;
        speedBtn.setEnabled(false);
        speedResultView.setText("Измерение…");
        upView.setText("");
        jitView.setText("");
        lossView.setText("");
        speedServerView.setText("");
        if (gaugeView != null) { gaugeView.setMax(200); gaugeView.setSpeed(0); }
        pool.execute(new Runnable() {
            @Override public void run() {
                long bestBytes = 0, bestNs = 0;
                String bestName = "";
                for (String u : SPEED_URLS) {
                    long[] r = downloadMeasure(u, 8_000_000L, 6000);
                    if (r != null && r[0] > bestBytes) {
                        bestBytes = r[0]; bestNs = r[1]; bestName = hostOf(u);
                    }
                }
                final double dl = bestNs > 0 ? bestBytes * 8.0 / (bestNs / 1e9) / 1e6 : 0;
                final String fname = bestName;

                long[] up = uploadMeasure(SPEED_UPLOAD_URL, 4_000_000L, 6000);
                final double ul = up != null && up[1] > 0
                        ? up[0] * 8.0 / (up[1] / 1e9) / 1e6 : 0;

                // джиттер и потери: 10 замеров времени соединения
                int samples = 10;
                long prev = -1, sumDiff = 0, diffs = 0;
                int lost = 0;
                for (int i = 0; i < samples; i++) {
                    Long t = tcpTime("google.com");
                    if (t == null) { lost++; continue; }
                    if (prev >= 0) { sumDiff += Math.abs(t - prev); diffs++; }
                    prev = t;
                }
                final double jitter = diffs > 0 ? (double) sumDiff / diffs : 0;
                final int loss = (int) Math.round(lost * 100.0 / samples);

                ui.post(new Runnable() {
                    @Override public void run() {
                        if (dl > 0) {
                            speedResultView.setText(
                                    String.format(Locale.US, "↓ %.1f Мбит/с", dl));
                            prefs.edit().putString("last_speed",
                                    String.format(Locale.US, "↓ %.1f Мбит/с", dl)).apply();
                            updatePersistentNotification();
                        } else {
                            speedResultView.setText("не удалось замерить");
                        }
                        if (gaugeView != null) gaugeView.setSpeed(dl);
                        upView.setText(ul > 0
                                ? String.format(Locale.US, "↑ %.1f Мбит/с", ul) : "—");
                        jitView.setText(String.format(Locale.US, "Джиттер: %.1f мс", jitter));
                        lossView.setText("Потери: " + loss + "%");
                        speedServerView.setText(fname);
                        speedBtn.setEnabled(true);
                    }
                });
            }
        });
    }

    private static Long tcpTime(String host) {
        long t0 = System.nanoTime();
        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(host, 443), 2000);
            return (System.nanoTime() - t0) / 1_000_000;
        } catch (Exception e) {
            return null;
        } finally {
            try { if (s != null) s.close(); } catch (Exception ignored) {}
        }
    }

    private static long[] uploadMeasure(String u, long maxBytes, int timeoutMs) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(u).openConnection();
            c.setConnectTimeout(3000);
            c.setReadTimeout(timeoutMs);
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setFixedLengthStreamingMode(maxBytes);
            c.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) CheckConnection/1.12.6");
            long t0 = System.nanoTime();
            OutputStream os = c.getOutputStream();
            byte[] buf = new byte[8192];
            java.util.Arrays.fill(buf, (byte) 'x');
            long total = 0;
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (total < maxBytes && System.currentTimeMillis() < deadline) {
                int n = (int) Math.min(buf.length, maxBytes - total);
                os.write(buf, 0, n);
                total += n;
            }
            os.close();
            c.getResponseCode();
            return new long[]{total, System.nanoTime() - t0};
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static String hostOf(String u) {
        try { return new URL(u).getHost(); } catch (Exception e) { return u; }
    }

    private static long[] downloadMeasure(String u, long maxBytes, int timeoutMs) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(u).openConnection();
            c.setConnectTimeout(3000);
            c.setReadTimeout(timeoutMs);
            c.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) CheckConnection/1.12");
            long t0 = System.nanoTime();
            InputStream in = c.getInputStream();
            byte[] buf = new byte[8192];
            long total = 0;
            long deadline = System.currentTimeMillis() + timeoutMs;
            int n;
            while ((n = in.read(buf)) > 0) {
                total += n;
                if (total >= maxBytes || System.currentTimeMillis() > deadline) break;
            }
            in.close();
            return new long[]{total, System.nanoTime() - t0};
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    // ===== уведомления =====
    private void toggleNotifications() {
        notifOn = !notifOn;
        prefs.edit().putBoolean("notif", notifOn).apply();
        if (notifOn) {
            ensureChannel();
            if (Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                        != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
            }
        }
        notifyBtn.setText(notifOn ? "Уведомления ✓" : "Уведомления");
        notifyBtn.setTextColor(notifOn ? accent : grey);
        updatePersistentNotification();
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(NOTIF_CHANNEL,
                    "Статус", NotificationManager.IMPORTANCE_MIN);
            ch.setDescription("Постоянный статус: скорость и IP");
            ch.setSound(null, null);
            nm.createNotificationChannel(ch);
        }
    }

    // постоянное тихое закреплённое уведомление: скорость + глубокий IP со страной
    private void updatePersistentNotification() {
        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (!notifOn) { nm.cancel(1); return; }
        ensureChannel();
        StringBuilder text = new StringBuilder();
        String ip = prefs.getString("deep_ip", "");
        if (ip.length() > 0) {
            text.append("IP: ").append(ip);
            String cc = prefs.getString("deep_country", "");
            if (cc.length() > 0) text.append(" (").append(countryName(cc)).append(")");
        }
        String sp = prefs.getString("last_speed", "");
        if (sp.length() > 0) {
            if (text.length() > 0) text.append('\n');
            text.append(sp);
        }
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            b = new Notification.Builder(this, NOTIF_CHANNEL);
        } else {
            b = new Notification.Builder(this);
            b.setPriority(Notification.PRIORITY_MIN);
        }
        b.setSmallIcon(R.mipmap.ic_launcher_0)
                .setContentTitle("Check Connection")
                .setContentText(text.length() > 0 ? text.toString() : "…")
                .setStyle(new Notification.BigTextStyle().bigText(text.toString()))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false);
        nm.notify(1, b.build());
    }

    private static String countryName(String cc) {
        String n = COUNTRY_RU.get(cc);
        return n != null ? n : cc;
    }


    // ===== управление сервисами =====
    private void showRenameDialog(final String host) {
        final EditText input = new EditText(this);
        input.setText(nameOf(host));
        input.setTextColor(text);
        input.setHintTextColor(grey);
        input.setSingleLine(true);
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(this)
                .setTitle("Название для " + host)
                .setView(input)
                .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        String v = input.getText().toString().trim();
                        if (v.length() > 0) renames.put(host, v);
                        else renames.remove(host);
                        saveCustom();
                        renderLists();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showAddDialog() {
        final EditText nameInput = new EditText(this);
        nameInput.setHint("Название");
        nameInput.setTextColor(text);
        nameInput.setHintTextColor(grey);
        nameInput.setSingleLine(true);

        final EditText hostInput = new EditText(this);
        hostInput.setHint("Адрес (example.com)");
        hostInput.setTextColor(text);
        hostInput.setHintTextColor(grey);
        hostInput.setSingleLine(true);
        hostInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(d(20), d(8), d(20), 0);
        box.addView(nameInput);
        box.addView(hostInput);

        new AlertDialog.Builder(this)
                .setTitle("Добавить сайт")
                .setView(box)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        String nm = nameInput.getText().toString().trim();
                        String hs = hostInput.getText().toString().trim().toLowerCase();
                        if (hs.length() > 0) {
                            if (nm.length() == 0) nm = hs;
                            customServices.add(new String[]{nm, hs});
                            saveCustom();
                            renderLists();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDeleteDialog(final String host) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить?")
                .setMessage("Точно хотите удалить " + nameOf(host) + " • " + host + "?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        if (isCustom(host)) {
                            for (int i = 0; i < customServices.size(); i++)
                                if (customServices.get(i)[1].equals(host))
                                    customServices.remove(i);
                        } else {
                            deleted.add(host);
                        }
                        stars.remove(host);
                        saveCustom();
                        saveStars();
                        renderLists();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ===== сводка и экспорт =====
    private void updateSummary() {
        if (summaryView == null) return;
        int green = 0, yellow = 0, red = 0;
        for (String h : allHosts()) {
            Integer code = resultCodes.get(h);
            if (code == null || code <= 0) { red++; continue; }
            Integer ms = resultMs.get(h);
            if (ms != null && ms <= GOOD_MS) green++; else yellow++;
        }
        summaryView.setText("Всего " + allHosts().size() + " · быстрых " + green
                + " · средних " + yellow + " · недоступно " + red);
    }

    private void shareResults() {
        StringBuilder sb = new StringBuilder("Check Connection\n");
        String ip = prefs.getString("deep_ip", "");
        if (ip.length() > 0) {
            sb.append("IP: ").append(ip);
            String cc = prefs.getString("deep_country", "");
            if (cc.length() > 0) sb.append(" (").append(countryName(cc)).append(")");
            sb.append('\n');
        }
        for (String cat : CATEGORIES) {
            for (String h : loadOrder("cat_" + cat)) {
                if (stars.contains(h) || !inCategory(h, cat)) continue;
                String t = resultTexts.get(h);
                if (t == null) continue;
                sb.append(nameOf(h)).append(" — ").append(t).append('\n');
            }
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Check Connection", sb.toString()));
        Toast.makeText(this, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    // ===== виджет =====
    private void updateWidget() {
        int green = 0, yellow = 0, red = 0;
        for (String h : allHosts()) {
            Integer code = resultCodes.get(h);
            if (code == null || code <= 0) { red++; continue; }   // недоступен
            Integer ms = resultMs.get(h);
            if (ms != null && ms <= GOOD_MS) green++;
            else yellow++;
        }
        String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        prefs.edit()
                .putString("w_g", "● " + green + " быстрых")
                .putString("w_y", "● " + yellow + " средних")
                .putString("w_r", "● " + red + " недоступно")
                .putString("w_time", "обновлено " + time)
                .apply();
        StatusWidget.push(this);
    }

    private static class Counter {
        private int n;
        synchronized int next() { return ++n; }
    }

    private static String flagEmoji(String cc) {
        if (cc == null || cc.length() != 2) return "";
        char a = Character.toUpperCase(cc.charAt(0));
        char b = Character.toUpperCase(cc.charAt(1));
        if (a < 'A' || a > 'Z' || b < 'A' || b > 'Z') return "";
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(0x1F1E6 + (a - 'A'));
        sb.appendCodePoint(0x1F1E6 + (b - 'A'));
        return sb.toString();
    }

    private static String jsonField(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static String fetch(String u) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(u).openConnection();
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            c.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) VPNCheck/1.2");
            InputStream in = c.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
            r.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    // настоящий HTTPS-запрос: время = DNS + TCP + TLS + ответ сервера.
    // Редиректы следуем вручную с лимитом (у части сайтов они зациклены).
    // Возвращает код ответа и время; null — если сервер не ответил.
    private static CheckResult httpCheck(String host) {
        long t0 = System.nanoTime();
        URL url = null;
        HttpURLConnection c = null;
        try {
            url = new URL("https://" + host + "/");
            int redirects = 0;
            int code;
            while (true) {
                c = (HttpURLConnection) url.openConnection();
                c.setConnectTimeout(2500);
                c.setReadTimeout(3000);
                c.setRequestMethod("GET");
                c.setInstanceFollowRedirects(false);
                c.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Linux; Android 13) CheckConnection/1.12");
                c.setRequestProperty("Accept", "*/*");
                code = c.getResponseCode();
                if (code == 301 || code == 302 || code == 303
                        || code == 307 || code == 308) {
                    if (redirects >= 1) break;
                    String loc = c.getHeaderField("Location");
                    c.disconnect();
                    c = null;
                    if (loc == null) break;
                    url = new URL(url, loc);
                    redirects++;
                    continue;
                }
                break;
            }
            if (code <= 0) return null;
            int ms = (int) ((System.nanoTime() - t0) / 1000000);
            InputStream in = (code >= 400) ? c.getErrorStream() : c.getInputStream();
            if (in != null) {
                byte[] buf = new byte[256];
                in.read(buf);
                in.close();
            }
            return new CheckResult(code, ms);
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static class CheckResult {
        final int code;
        final int ms;
        CheckResult(int code, int ms) { this.code = code; this.ms = ms; }
    }
}
