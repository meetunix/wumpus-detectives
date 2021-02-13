curl -H "accept: application/json" 'http://127.0.0.1:12345/wumpus/worldstate' > sample_world_state.json

curl -H "accept: application/json" 'http://127.0.0.1:12345/wumpus/worldstate?human=true' > sample_world_state_hr.json

curl -H "accept: application/json" -H "accept-encoding: gzip" 'http://127.0.0.1:12345/wumpus/worldstate' > sample_world_state.json.gzip
