import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def jsonSlurper = new JsonSlurper()
def obj = jsonSlurper.parseText(payload)
def stockPrice = obj.price

if (stockPrice > 100) {
  obj.action = "SELL"
} else {
  obj.action = "HOLD"
}

return JsonOutput.toJson(obj)
