function engagementFunction() {
    document.getElementById("token").value = localStorage.getItem('TOKEN') ? localStorage.getItem('TOKEN') : '';
    $.ajax({
        type: "GET",
        url: '/engagements',
        beforeSend: function (xhr) {
          xhr.setRequestHeader('Authorization', 'Bearer ' + document.getElementById("token").value);
        },
        success: function(data, status) {
            var eng = Object.values(data);
            //console.log(JSON.stringify(eng) + " and status is " + status)
            div = document.getElementById("myDropdown");
            var elements = document.getElementsByTagName('a');
            for (var i = elements.length; i-- > 0;) {
                var element = elements[i];
                element.parentNode.removeChild(element);
            }
            input = document.getElementById("myInput");
            uuid = document.getElementById("uuid");
            for (i = 0; i < eng.length; i++) {
                var node = document.createElement("a");
                var textnode;
                textnode = document.createTextNode(eng[i].name);
                node.deets = eng[i];
                node.onclick = function() {
                    input.value = this.deets.name.replace(/\"/g, "")
                    uuid.value = this.deets.uuid.replace(/\"/g, "")
                    var elements = document.getElementsByTagName('a');
                    for (var i = elements.length; i-- > 0;) {
                        var element = elements[i];
                        element.parentNode.removeChild(element);
                    }
                };
                node.appendChild(textnode);
                div.appendChild(node);
            }
            var n = document.createElement("a");
            t = document.createTextNode("ALL")
            n.appendChild(t);
            n.onclick = function() {
                input.value = "ALL"
                uuid.value = "999"
                for (var i = elements.length; i-- > 0;) {
                    var element = elements[i];
                    element.parentNode.removeChild(element);
                }
            };
            div.appendChild(n);
            filter = input.value.toUpperCase();
            a = div.getElementsByTagName("a");
            for (i = 0; i < a.length; i++) {
                txtValue = a[i].textContent || a[i].innerText;
                if (txtValue.toUpperCase().indexOf(filter) > -1) {
                    a[i].style.display = "";
                } else {
                    a[i].style.display = "none";
                }
            }
        }
    });
}
function setTokenInStorage () {
    let token = document.getElementById('token').value.toLowerCase();
    localStorage.setItem('TOKEN', token);
}
function clearStorage () {
    localStorage.removeItem('TOKEN');
    document.getElementById('token').value = '';
}
