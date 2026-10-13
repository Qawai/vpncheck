package com.qawai.vpncheck;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String[] IP_SOURCES = {
            "https://2ip.ru/ip/",
            "https://api.ipify.org",
            "https://ipinfo.io/ip",
            "https://ifconfig.me/ip"
    };

    private static final String[][] SERVICES = {
            {"Discord", "discord.com"},
            {"Discord CDN", "cdn.discordapp.com"},
            {"Telegram", "api.telegram.org"},
            {"YouTube", "youtube.com"},
            {"Google", "google.com"},
            {"Yandex", "ya.ru"},
            {"Yandex Disk", "disk.yandex.ru"},
            {"VK", "vk.com"},
            {"Mail.ru", "mail.ru"},
            {"Steam", "store.steampowered.com"},
            {"Steam CDN", "cdn.cloudflare.steamstatic.com"},
            {"GitHub", "github.com"},
            {"Twitch", "www.twitch.tv"},
            {"Cloudflare", "1.1.1.1"},
            {"Cloudflare DNS", "one.one.one.one"},
            {"Wildberries", "www.wildberries.ru"},
            {"Ozon", "www.ozon.ru"},
            {"Reddit", "www.reddit.com"},
            {"X (Twitter)", "x.com"},
            {"Instagram", "www.instagram.com"},
            {"Facebook", "www.facebook.com"},
            {"TikTok", "www.tiktok.com"},
            {"Netflix", "www.netflix.com"},
            {"Spotify", "open.spotify.com"},
            {"Wikipedia", "www.wikipedia.org"},
            {"Roblox", "www.roblox.com"},
            {"Zoom", "zoom.us"},
            {"OpenAI", "api.openai.com"}
    };

    private static final Pattern IP_RE =
            Pattern.compile("\\b(\\d{1,3}\\.){3}\\d{1,3}\\b");

    private static final String[] THEME_NAMES =
            {"Тёмная", "Чёрная (AMOLED)", "Светлая", "Терминал"};
    private static final int[][] THEMES = {
            // bg, text, accent, grey, good, warn, bad
            {0xFF101418, 0xFFE8EAED, 0xFF4FC3F7, 0xFF9AA0A6,
             0xFF66BB6A, 0xFFFFD54F, 0xFFEF5350},
            {0xFF000000, 0xFFE8EAED, 0xFF00E676, 0xFF757575,
             0xFF00E676, 0xFFFFD54F, 0xFFEF5350},
            {0xFFF5F5F5, 0xFF212121, 0xFF1565C0, 0xFF757575,
             0xFF2E7D32, 0xFFF9A825, 0xFFC62828},
            {0xFF0D1117, 0xFF3FB950, 0xFF3FB950, 0xFF8B949E,
             0xFF3FB950, 0xFFD29922, 0xFFF85149}
    };

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final ExecutorService pool = Executors.newFixedThreadPool(8);
    private final Runnable autoRefresh = new Runnable() {
        @Override public void run() {
            refresh();
            ui.postDelayed(this, 45000);
        }
    };

    private TextView ipView, statusView, infoView, themeBtn;
    private ProgressBar progress;
    private Button btn;
    private LinearLayout pingContainer;
    private final List<LinearLayout> rows = new ArrayList<LinearLayout>();
    private final List<TextView> rowValues = new ArrayList<TextView>();
    private boolean busy;
    private float dp;
    private int themeIdx;

    private int bg, text, accent, grey, good, warn, bad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dp = getResources().getDisplayMetrics().density;
        themeIdx = getSharedPreferences("vpncheck", MODE_PRIVATE)
                .getInt("theme", 0);
        applyTheme();
        buildUi();
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
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(d(16), d(16), d(16), d(24));
        scroll.addView(root);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.addView(label("VPN Check", 24, accent, true));
        TextView ver = label("  v1.1", 12, grey, false);
        head.addView(ver);
        root.addView(head);

        ipView = label("IP: ...", 18, text, true);
        ipView.setTypeface(Typeface.MONOSPACE);
        ipView.setPadding(0, d(14), 0, 0);
        root.addView(ipView);

        statusView = label("", 15, text, false);
        statusView.setPadding(0, d(6), 0, 0);
        root.addView(statusView);

        infoView = label("", 14, grey, false);
        infoView.setPadding(0, d(2), 0, 0);
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
        themeBtn.setText("Тема: " + THEME_NAMES[themeIdx] + "  ▸");
        themeBtn.setTextColor(accent);
        themeBtn.setTextSize(15);
        themeBtn.setPadding(d(20), d(14), 0, d(14));
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                themeIdx = (themeIdx + 1) % THEMES.length;
                getSharedPreferences("vpncheck", MODE_PRIVATE)
                        .edit().putInt("theme", themeIdx).apply();
                recreate();
            }
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

        pingContainer = new LinearLayout(this);
        pingContainer.setOrientation(LinearLayout.VERTICAL);
        pingContainer.setPadding(0, d(6), 0, 0);
        root.addView(pingContainer);

        for (String[] s : SERVICES) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, d(3), 0, d(3));

            TextView name = label(s[0], 13, text, false);
            name.setMinWidth(d(130));
            row.addView(name);

            TextView val = label("…", 13, grey, false);
            val.setTypeface(Typeface.MONOSPACE);
            row.addView(val);

            pingContainer.addView(row);
            rows.add(row);
            rowValues.add(val);
        }

        setContentView(scroll);
    }

    private void refresh() {
        if (busy) return;
        busy = true;
        btn.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        for (TextView v : rowValues) {
            v.setText("…");
            v.setTextColor(grey);
        }

        final SharedPreferences prefs =
                getSharedPreferences("vpncheck", MODE_PRIVATE);

        pool.execute(new Runnable() {
            @Override public void run() {
                // --- IP ---
                String ip = null, source = null;
                for (String u : IP_SOURCES) {
                    String r = fetch(u);
                    if (r == null) continue;
                    Matcher m = IP_RE.matcher(r);
                    if (m.find()) { ip = m.group(); source = hostOf(u); break; }
                }
                final String last = prefs.getString("last_ip", null);
                final boolean changed =
                        ip != null && last != null && !last.equals(ip);

                String org = "", country = "", city = "";
                String js = fetch("https://ipinfo.io/json");
                if (js != null) {
                    org = jsonField(js, "org");
                    country = jsonField(js, "country");
                    city = jsonField(js, "city");
                }
                if (ip != null) prefs.edit().putString("last_ip", ip).apply();
                final String fOrg = org, fCountry = country, fCity = city;
                final String fIp = ip, fSrc = source;

                ui.post(new Runnable() {
                    @Override public void run() {
                        if (fIp == null) {
                            ipView.setText("IP: не определён");
                            ipView.setTextColor(bad);
                            statusView.setText("Нет интернета или сервисы недоступны");
                            statusView.setTextColor(bad);
                        } else {
                            ipView.setText("IP: " + fIp);
                            ipView.setTextColor(text);
                            if (last == null) {
                                statusView.setText("Предыдущий IP не сохранён");
                                statusView.setTextColor(grey);
                            } else if (changed) {
                                statusView.setText("IP изменился: " + last + " → " + fIp);
                                statusView.setTextColor(good);
                            } else {
                                statusView.setText("IP не изменился: " + fIp);
                                statusView.setTextColor(bad);
                            }
                        }
                        String meta = "источник: " + (fSrc == null ? "-" : fSrc);
                        if (fOrg.length() > 0 || fCountry.length() > 0) {
                            meta += "\n" + fOrg
                                    + (fCity.length() > 0 ? " · " + fCity : "")
                                    + (fCountry.length() > 0 ? " · " + fCountry : "");
                        }
                        infoView.setText(meta);
                    }
                });

                // --- пинги, результаты появляются по мере ответа ---
                final int n = SERVICES.length;
                final int[] mss = new int[n];
                final boolean[] ok = new boolean[n];
                final AtomicInteger done = new AtomicInteger(0);

                for (int i = 0; i < n; i++) {
                    final int idx = i;
                    pool.execute(new Runnable() {
                        @Override public void run() {
                            Integer v = tcpPing(SERVICES[idx][1], 443, 3);
                            synchronized (ok) {
                                if (v != null) { mss[idx] = v; ok[idx] = true; }
                            }
                            final int finished = done.incrementAndGet();
                            ui.post(new Runnable() {
                                @Override public void run() {
                                    TextView val = rowValues.get(idx);
                                    if (ok[idx]) {
                                        int ms = mss[idx];
                                        val.setText(ms + " ms");
                                        val.setTextColor(
                                                ms <= 80 ? good : ms <= 150 ? warn : bad);
                                    } else {
                                        val.setText("нет ответа");
                                        val.setTextColor(bad);
                                    }
                                    if (finished == n) {
                                        sortRows(mss, ok, n);
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

    private void sortRows(final int[] mss, final boolean[] ok, int n) {
        List<int[]> order = new ArrayList<int[]>();
        for (int i = 0; i < n; i++)
            order.add(new int[]{i, ok[i] ? mss[i] : 999999});
        Collections.sort(order, new Comparator<int[]>() {
            @Override public int compare(int[] a, int[] b) { return a[1] - b[1]; }
        });
        pingContainer.removeAllViews();
        for (int[] p : order) pingContainer.addView(rows.get(p[0]));
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
                    "Mozilla/5.0 (Linux; Android 13) VPNCheck/1.1");
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

    private static Integer tcpPing(String host, int port, int tries) {
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < tries; i++) {
            Socket s = new Socket();
            try {
                long t0 = System.nanoTime();
                s.connect(new InetSocketAddress(host, port), 2500);
                int ms = (int) ((System.nanoTime() - t0) / 1000000);
                if (ms < best) best = ms;
            } catch (Exception e) {
                // следующая попытка
            } finally {
                try { s.close(); } catch (Exception ignored) {}
            }
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        return best == Integer.MAX_VALUE ? null : best;
    }
}
