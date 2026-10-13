#!/data/data/com.termux/files/usr/bin/bash
# vpncheck.sh - пинг важных сервисов (после подключения VPN)
# Запуск: bash vpncheck.sh   или   ./vpncheck.sh

set -u

RED='\033[91m'; YEL='\033[93m'; GRN='\033[92m'; CYA='\033[96m'
DIM='\033[2m'; BLD='\033[1m'; OFF='\033[0m'
[[ -t 1 ]] || { RED=''; YEL=''; GRN=''; CYA=''; DIM=''; BLD=''; OFF=''; }

# имя|хост|порт
TARGETS=(
  "Discord|discord.com|443"
  "Telegram|api.telegram.org|443"
  "YouTube|youtube.com|443"
  "Google|google.com|443"
  "Cloudflare|1.1.1.1|443"
  "GitHub|github.com|443"
  "Steam|store.steampowered.com|443"
  "Twitch|www.twitch.tv|443"
  "Yandex|ya.ru|443"
  "VK|vk.com|443"
  "Mail.ru|mail.ru|443"
  "Wildberries|www.wildberries.ru|443"
  "Ozon|www.ozon.ru|443"
  "OpenAI|api.openai.com|443"
)

COUNT=4          # пингов на цель
WAIT=2           # таймаут ответа, сек
JOBS=8           # параллельных проверок

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

color_ms() {
  local v="${1%%.*}"
  [[ "$v" =~ ^[0-9]+$ ]] || { echo "$1"; return; }
  if   (( v <= 80  )); then echo -e "${GRN}$1${OFF}"
  elif (( v <= 150 )); then echo -e "${YEL}$1${OFF}"
  else                      echo -e "${RED}$1${OFF}"
  fi
}

bar() {
  local ms="${1%%.*}"; [[ "$ms" =~ ^[0-9]+$ ]] || { printf '%s' "----"; return; }
  local n=$(( ms / 15 )); (( n > 10 )) && n=10; (( n < 1 )) && n=1
  local s="" i
  for (( i=0; i<n; i++ )); do s+="█"; done
  for (( i=n; i<10; i++ )); do s+="${DIM}█${OFF}"; done
  printf '%s' "$s"
}

tcp_ping() {  # $1=host $2=port -> мс или пусто
  local h="$1" p="$2" t0 t1
  t0=$(date +%s%N) || return 1
  timeout 2 bash -c "exec 3<>/dev/tcp/$h/$p" 2>/dev/null || return 1
  t1=$(date +%s%N)
  echo $(( (t1 - t0) / 1000000 ))
}

echo
echo -e "${BLD}=== Проверка VPN: внешний IP ===${OFF}"
IP=$(curl -m 5 -s https://ipinfo.io/ip 2>/dev/null || curl -m 5 -s https://api.ipify.org 2>/dev/null)
ORG=$(curl -m 5 -s https://ipinfo.io/org 2>/dev/null)
CC=$(curl -m 5 -s https://ipinfo.io/country 2>/dev/null)
if [[ -n "${IP:-}" ]]; then
  echo -e " IP:      ${CYA}${IP}${OFF}"
  echo -e " Страна:  ${CYA}${CC:--}${OFF}"
  echo -e " Провайдер/VPN: ${CYA}${ORG:--}${OFF}"
  echo -e "${DIM}Если это не ваш реальный IP/провайдер - VPN работает.${OFF}"
else
  echo -e " ${YEL}Не удалось определить внешний IP (нет интернета?)${OFF}"
fi

echo
echo -e "${BLD}=== Пинг сервисов (${COUNT} запроса на цель) ===${OFF}"
printf "${DIM}%-12s %-26s %7s %7s %7s %7s %6s  %s${OFF}\n" \
  "СЕРВИС" "ХОСТ" "МИН" "СР" "МАКС" "ДЖИТТР" "ПОТЕРИ" "ГРАФИК"

i=0
for t in "${TARGETS[@]}"; do
  IFS='|' read -r name host port <<< "$t"
  (
    ping -c "$COUNT" -W "$WAIT" "$host" > "$TMP/$i.raw" 2>&1
    if ! grep -q "rtt min" "$TMP/$i.raw" && ! grep -q "round-trip" "$TMP/$i.raw"; then
      v=$(tcp_ping "$host" "$port")
      [[ -n "$v" ]] && echo "TCP $v" > "$TMP/$i.raw"
    fi
  ) &
  (( i++ >= JOBS )) && wait -n 2>/dev/null || true
done
wait

best=99999; best_name=""; worst=0; worst_name=""
i=0
for t in "${TARGETS[@]}"; do
  IFS='|' read -r name host port <<< "$t"
  f="$TMP/$i.raw"
  out=$(grep -E "rtt min|round-trip" "$f" 2>/dev/null | tail -1)
  loss=$(grep -oE "[0-9.]+% packet loss" "$f" 2>/dev/null | tail -1 | grep -oE "^[0-9.]+")
  if [[ -n "$out" ]]; then
    IFS='/' read -r mn av mx jt <<< "$(echo "$out" | sed -E 's/.*= *//; s/ ms.*//')"
    meth="ICMP"
  elif grep -q "^TCP " "$f" 2>/dev/null; then
    av=$(grep -oE "TCP [0-9]+" "$f" | awk '{print $2}')
    mn=$av; mx=$av; jt=0; loss=0; meth="TCP"
  else
    printf "%-12s %-26s %s\n" "$name" "$host" "${RED}нет ответа / заблокировано${OFF}"
    worst=9999; worst_name="$name"
    (( i++ )); continue
  fi
  [[ -z "${loss:-}" ]] && loss=0
  c_av=$(color_ms "$av")
  printf "%-12s %-26s %7s %b %7s %7s %5s%%  %b" \
    "$name" "$host" "$mn" "$c_av" "$mx" "$jt" "$loss" "$(bar "$av")"
  [[ "$meth" == "TCP" ]] && printf " ${DIM}(TCP 443, ICMP запрещён)${OFF}"
  printf "\n"
  av_n=${av%%.*}; [[ "$av_n" =~ ^[0-9]+$ ]] && av_n=99999
  if (( av_n < best )); then best=$av_n; best_name="$name"; fi
  if (( av_n > worst )); then worst=$av_n; worst_name="$name"; fi
  (( i++ ))
done

echo
if [[ -n "$best_name" ]]; then
  echo -e " Быстрее всего:  ${GRN}$best_name${OFF} (${best} мс)"
  echo -e " Медленнее всего: ${RED}$worst_name${OFF} (${worst} мс)"
fi
echo -e "${DIM}Цвет: ${GRN}<=80 мс${OFF}${DIM} | ${YEL}<=150 мс${OFF}${DIM} | ${RED}>150 мс${OFF}"
echo
