var div_body_list = document.getElementById("list");
var strLine;
for(var i=1;i<10;i++){
    strLine = "i="+i+"<br>";
    console.log(strLine);
    div_body_list.innerHTML +=strLine;
}