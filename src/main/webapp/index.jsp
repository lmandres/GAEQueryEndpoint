<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <link href='//fonts.googleapis.com/css?family=Marmelad' rel='stylesheet' type='text/css'>
 <title>Hello App Engine Standard Java 8</title>
 <script type='text/javascript'>
// Sending and receiving data in JSON format using POST method
//
var xhr = new XMLHttpRequest();
var url = "http://localhost:8080/query";
xhr.open("POST", url, true);
xhr.setRequestHeader("Content-Type", "application/json");
xhr.onreadystatechange = function () {
    if (xhr.readyState === 4 && xhr.status === 200) {
        var json = JSON.parse(xhr.responseText);
        console.log(json.email + ", " + json.password);
    }
};
var data = JSON.stringify(
    {
        "email": "hey@mail.com",
        "password": "101010",
        "true": true,
        "false": false,
        "null": null,
        "integer": 130,
        "float": 5.325,
        "string": "Some String value",
        "list": [
            1,
            2,
            3,
            4,
            5
        ],
        "dict": {
            "test1": "value1",
            "test2": "value2",
            "test3": "value3"
        },
        "dictlist": [
            {"dictlist1": "value1"},
            {"dictlist2": "value2"}
        ],
        "listdict": {
            "listdict1": ["listdictitem1", "listdictitem2"],
            "listdict2": ["listdictitem3", "listdictitem4"]
        }
    }
);
console.log(data);
xhr.send(data);
 </script>
</head>
<body>
    <h1>Hello App Engine -- Java 8!</h1>
  <table>
    <tr>
      <td colspan="2" style="font-weight:bold;">Available Servlets:</td>
    </tr>
    <tr>
      <td><a href='/hello'>Hello App Engine</a></td>
    </tr>
  </table>


</body>
</html>
