```
curl -v localhost:9001 -d '{
  "node": {
    "cpuIntensity": 1,
    "responseSize": 1,
    "collaborators": [
      {
        "url": "http://localhost:9002",
        "requestSize": 1,
        "node": {
          "cpuIntensity": 30,
          "responseSize": 0,
          "collaborators": [
            {
              "url": "http://localhost:9003",
              "requestSize": 1,
              "node": {
                "cpuIntensity": 1,
                "responseSize": 5
              }
            }
          ]
        }
      }
    ]
  }
}'
```