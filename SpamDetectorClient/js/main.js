// TODO: onload function should retrieve the data needed to populate the UI
function add_record_API(tableID, data) {

  for (c in data.testFiles){
    const tr = `<tr>
                    <td>${data.testFiles[c].name}</td>
                    <td>${data.testFiles[c].id}</td>
                    <td>${data.testFiles[c].gpa}</td>
                    </tr>`;

    document.getElementById("chart").innerHTML += tr;
  }

  console.log(data);
}

let callURL = "http://localhost:8080/lab5-1.0/api/students/json";

function requestData(callURL){
  fetch(callURL, {
    method: 'GET',
    headers: {
      'Accept': 'application/json',
    },
  })
    .then(response => response.json())
    .then(response => add_record_API("chart", response))
    .catch((err) => {
      console.log("something went wrong: " + err);
    });
}

(function () {
  requestData(callURL);
})();


