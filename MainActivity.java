package com.qawai.vpncheck;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String[] IP_SOURCES = {
            "https://2ip.ru/ip/",
            "https://api.ipify.org",
            "https://ipinfo.io/ip",
            "https://ifconfig.me/ip"
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
            "Магазины", "Игры", "Сеть"
    };

    private static final Pattern IP_RE =
            Pattern.compile("\\b(\\d{1,3}\\.){3}\\d{1,3}\\b");

    private static final String[] THEME_NAMES = {
            "Тёмная", "Чёрная (AMOLED)", "Светлая", "Терминал",
            "Мягкий светлый", "Мягкий тёмный", "Лаванда", "Мята"
    };
    private static final boolean[] IS_LIGHT = {
            false, false, true, false, true, false, false, false
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

    // пороги пинга: до 500 мс — зелёная зона, до 1800 — жёлтая, дальше красная
    private static final int GOOD_MS = 500;
    private static final int WARN_MS = 1800;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final Runnable autoRefresh = new Runnable() {
        @Override public void run() {
            refresh();
            ui.postDelayed(this, 45000);
        }
    };

    private TextView ipView, infoView, themeBtn;
    private ProgressBar progress;
    private Button btn;
    private LinearLayout listRoot, pinnedContainer;
    private final Map<String, LinearLayout> catContainers =
            new LinkedHashMap<String, LinearLayout>();
    private final Map<String, TextView> valueViews =
            new LinkedHashMap<String, TextView>();
    private final Map<String, String> resultTexts =
            new LinkedHashMap<String, String>();
    private final Map<String, Integer> resultMs = new LinkedHashMap<String, Integer>();

    private Set<String> stars = new HashSet<String>();
    private boolean busy;
    private float dp;
    private int themeIdx;

    private String dragHost;
    private View dragRow;
    private float startRawY;
    private PopupWindow popup;

    private int bg, text, accent, grey, good, warn, bad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dp = getResources().getDisplayMetrics().density;
        SharedPreferences p = getSharedPreferences("vpncheck", MODE_PRIVATE);
        themeIdx = p.getInt("theme", 0);
        stars = new HashSet<String>(p.getStringSet("stars", new HashSet<String>()));
        applyTheme();
        buildUi();
        renderLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        ui.removeCallbacks(autoRefresh);
        ui.postDelayed(autoRefresh, 45000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ui.removeCallbacks(autoRefresh);
    }

    private void applyTheme() {
        int[] t = THEMES[themeIdx];
        bg = t[0]; text = t[1]; accent = t[2]; grey = t[3];
        good = t[4]; warn = t[5]; bad = t[6];
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

    private void buildUi() {
        final ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(d(16), d(16), d(16), d(24));
        scroll.addView(root);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.addView(label("VPN Check", 24, accent, true));
        head.addView(label("  v1.4", 12, grey, false));
        root.addView(head);

        ipView = label("IP: ...", 18, text, true);
        ipView.setTypeface(Typeface.MONOSPACE);
        ipView.setPadding(0, d(14), 0, 0);
        root.addView(ipView);

        infoView = label("", 14, grey, false);
        infoView.setPadding(0, d(6), 0, 0);
        root.addView(infoView);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setPadding(0, d(14), 0, 0);

        btn = new Button(this);
        btn.setText("Обновить");
        btn.setTextColor(accent);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { refresh(); }
        });
        controls.addView(btn);

        themeBtn = new TextView(this);
        themeBtn.setText(THEME_NAMES[themeIdx] + "  ▾");
        themeBtn.setTextColor(accent);
        themeBtn.setTextSize(15);
        themeBtn.setPadding(d(20), d(14), 0, d(14));
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showThemeMenu(v); }
        });
        controls.addView(themeBtn);
        root.addView(controls);

        progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams pp =
                new LinearLayout.LayoutParams(-2, -2);
        pp.topMargin = d(4);
        root.addView(progress, pp);

        root.addView(label("Пинг сервисов", 17, accent, true));
        root.addView(label("тап — закрепить ★ · удержание — перетащить",
                12, grey, false));

        listRoot = new LinearLayout(this);
        listRoot.setOrientation(LinearLayout.VERTICAL);
        listRoot.setPadding(0, d(6), 0, 0);
        root.addView(listRoot);

        pinnedContainer = new LinearLayout(this);
        pinnedContainer.setOrientation(LinearLayout.VERTICAL);

        for (String cat : CATEGORIES) {
            catContainers.put(cat, new LinearLayout(this));
            catContainers.get(cat).setOrientation(LinearLayout.VERTICAL);
        }

        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (dragRow != null) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }
        });

        setContentView(scroll);
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
                    getSharedPreferences("vpncheck", MODE_PRIVATE)
                            .edit().putInt("theme", idx).apply();
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
        for (String h : pinned) if (stars.contains(h) && exists(h)) visPinned.add(h);
        for (String h : allHosts()) if (stars.contains(h) && !visPinned.contains(h))
            visPinned.add(h);

        if (!visPinned.isEmpty()) {
            listRoot.addView(sectionHeader("★ Закреплённые"));
            pinnedContainer.removeAllViews();
            listRoot.addView(pinnedContainer);
            for (String h : visPinned) pinnedContainer.addView(createRow(h));
        }

        for (String cat : CATEGORIES) {
            LinearLayout cont = catContainers.get(cat);
            cont.removeAllViews();
            for (String h : loadOrder("cat_" + cat)) {
                if (stars.contains(h) || !inCategory(h, cat)) continue;
                cont.addView(createRow(h));
            }
            if (cont.getChildCount() == 0) continue;
            listRoot.addView(sectionHeader(cat));
            listRoot.addView(cont);
        }
    }

    private TextView sectionHeader(String title) {
        TextView h = label(title, 15, accent, true);
        h.setPadding(0, d(14), 0, d(4));
        return h;
    }

    private LinearLayout createRow(final String host) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, d(4), d(4), d(4));
        row.setTag(host);
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (stars.contains(host)) stars.remove(host);
                else stars.add(host);
                saveStars();
                renderLists();
            }
        });
        row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
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
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startRawY = e.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE: {
                        float dy = e.getRawY() - startRawY;
                        dragRow.setTranslationY(dy);
                        LinearLayout parent = (LinearLayout) dragRow.getParent();
                        int idx = parent.indexOfChild(dragRow);
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
                        dragRow.setTranslationY(0);
                        dragRow.setAlpha(1f);
                        dragRow.setBackgroundColor(0);
                        saveOrder();
                        dragHost = null;
                        dragRow = null;
                        return true;
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
        return row;
    }

    private int colorFor(String host) {
        Integer ms = resultMs.get(host);
        if (ms == null) return bad;
        return ms <= GOOD_MS ? good : ms <= WARN_MS ? warn : bad;
    }

    private boolean exists(String host) {
        for (String[] s : SERVICES) if (s[1].equals(host)) return true;
        return false;
    }

    private String nameOf(String host) {
        for (String[] s : SERVICES) if (s[1].equals(host)) return s[0];
        return host;
    }

    private boolean inCategory(String host, String cat) {
        for (String[] s : SERVICES)
            if (s[1].equals(host) && s[2].equals(cat)) return true;
        return false;
    }

    private List<String> allHosts() {
        List<String> l = new ArrayList<String>();
        for (String[] s : SERVICES) l.add(s[1]);
        return l;
    }

    private List<String> loadOrder(String key) {
        SharedPreferences p = getSharedPreferences("vpncheck", MODE_PRIVATE);
        String saved = p.getString("order_" + key, "");
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
        return out;
    }

    private void saveOrder() {
        SharedPreferences.Editor ed =
                getSharedPreferences("vpncheck", MODE_PRIVATE).edit();
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
        getSharedPreferences("vpncheck", MODE_PRIVATE).edit()
                .putStringSet("stars", new LinkedHashSet<String>(stars)).apply();
    }

    private void refresh() {
        if (busy) return;
        busy = true;
        btn.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        resultTexts.clear();
        resultMs.clear();
        for (TextView v : valueViews.values()) {
            v.setText("…");
            v.setTextColor(grey);
        }

        pool.execute(new Runnable() {
            @Override public void run() {
                String ip = null, source = null;
                for (String u : IP_SOURCES) {
                    String r = fetch(u);
                    if (r == null) continue;
                    Matcher m = IP_RE.matcher(r);
                    if (m.find()) { ip = m.group(); source = hostOf(u); break; }
                }
                String org = "", country = "", city = "";
                String js = fetch("https://ipinfo.io/json");
                if (js != null) {
                    org = jsonField(js, "org");
                    country = jsonField(js, "country");
                    city = jsonField(js, "city");
                }
                final String fOrg = org, fCountry = country, fCity = city;
                final String fIp = ip, fSrc = source;

                ui.post(new Runnable() {
                    @Override public void run() {
                        if (fIp == null) {
                            ipView.setText("IP: не определён");
                            ipView.setTextColor(bad);
                            infoView.setText("Нет интернета или сервисы недоступны");
                        } else {
                            ipView.setText("IP: " + fIp);
                            ipView.setTextColor(text);
                            String meta = "источник: " + (fSrc == null ? "-" : fSrc);
                            if (fOrg.length() > 0 || fCountry.length() > 0) {
                                meta += "\n" + fOrg
                                        + (fCity.length() > 0 ? " · " + fCity : "")
                                        + (fCountry.length() > 0 ? " · " + fCountry : "");
                            }
                            infoView.setText(meta);
                        }
                    }
                });

                final List<String> hosts = allHosts();
                final AtomicIntegerMap done = new AtomicIntegerMap();
                for (final String host : hosts) {
                    pool.execute(new Runnable() {
                        @Override public void run() {
                            Integer v = httpCheck(host);
                            synchronized (resultMs) {
                                if (v != null) {
                                    resultMs.put(host, v);
                                    resultTexts.put(host, v + " мс");
                                } else {
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
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private static class AtomicIntegerMap {
        private int n;
        synchronized int next() { return ++n; }
    }

    private static String jsonField(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static String hostOf(String url) {
        try { return new URL(url).getHost(); } catch (Exception e) { return url; }
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
    // Если сервер не ответил за таймаут или соединение сорвалось -> недоступен.
    private static Integer httpCheck(String host) {
        long t0 = System.nanoTime();
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection)
                    new URL("https://" + host + "/").openConnection();
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            c.setRequestMethod("GET");
            c.setInstanceFollowRedirects(true);
            c.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) VPNCheck/1.3");
            c.setRequestProperty("Accept", "*/*");
            // код ответа = сервер реально отвечает
            int code = c.getResponseCode();
            if (code < 200) return null;
            int ms = (int) ((System.nanoTime() - t0) / 1000000);
            // прочитать немного тела, чтобы убедиться, что поток живой
            InputStream in = (code >= 400) ? c.getErrorStream() : c.getInputStream();
            if (in != null) {
                byte[] buf = new byte[256];
                in.read(buf);
                in.close();
            }
            return ms;
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }
}
