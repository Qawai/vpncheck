#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$HOME/apkbuild"

rm -rf classes dex gen app-unsigned.apk
mkdir -p classes dex gen

if [ ! -f android30.jar ]; then
  echo "Скачиваю android30.jar (API 30)..."
  curl -m 300 -L -o android30.jar \
    "https://repo1.maven.org/maven2/org/robolectric/android-all/11-robolectric-6757853/android-all-11-robolectric-6757853.jar"
fi

echo "[1/6] иконки (по темам)..."
if [ ! -f res/mipmap/ic_launcher_7.png ]; then
  javac -d . IconGen.java 2>/dev/null
  java -cp . IconGen res/mipmap
fi
rm -f res/mipmap/ic_launcher.png

echo "[2/6] aapt (ресурсы + R.java)..."
aapt package -M AndroidManifest.xml -S res -I android30.jar \
  -F app-unsigned.apk -J gen

echo "[3/6] javac..."
javac -source 1.8 -target 1.8 -classpath "android30.jar:gen" \
  -d classes \
  src/com/qawai/vpncheck/MainActivity.java \
  src/com/qawai/vpncheck/StatusWidget.java \
  gen/R.java 2>&1 | grep -v "bootstrap class path" || true

echo "[4/6] d8 -> dex..."
d8 --release --lib android30.jar --output dex $(find classes -name '*.class')

echo "[5/6] zip + подпись..."
(cd dex && zip -q ../app-unsigned.apk classes.dex)

if [ ! -f key.jks ]; then
  keytool -genkeypair -keystore key.jks -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass pass1234 -keypass pass1234 \
    -alias vpncheck -dname "CN=vpncheck" -noprompt
fi

apksigner sign --ks key.jks --ks-pass pass:pass1234 \
  --out "$HOME/vpncheck.apk" app-unsigned.apk

echo "[6/6] проверка..."
apksigner verify --verbose "$HOME/vpncheck.apk" | grep -E "Verified using|Signer"
ls -lh "$HOME/vpncheck.apk"
