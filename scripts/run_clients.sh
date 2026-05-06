#!/usr/bin/env bash
set -euo pipefail

COUNT=${1:-4}
STORE_PASSWORD=${2:-${GAMECLIENT_STORE_PASSWORD:-}}

if [[ -z "$STORE_PASSWORD" ]]; then
  echo "Missing GAMECLIENT_STORE_PASSWORD."
  echo "Usage: scripts/run_clients.sh [count] [password]"
  exit 1
fi

WORKDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

for i in $(seq 1 "$COUNT"); do
  (
    export GAMECLIENT_STORE_PASSWORD="$STORE_PASSWORD"
    cd "$WORKDIR"
    mvn clean javafx:run -Djavafx.mainClass=com.werewolf/com.werewolf.client.view.MainMenuView \
      >"/tmp/werewolf-client-$i.log" 2>&1
  ) &
  sleep 0.4
  echo "Started client $i (log: /tmp/werewolf-client-$i.log)"
 done

echo "Launched $COUNT clients. Set usernames manually in each window."
