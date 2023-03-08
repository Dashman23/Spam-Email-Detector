// TODO: onload function should retrieve the data needed to populate the UI
function add_record_API(tableID, data) {

  for (c in data){
    const tr = `<tr>
                    <td>${data[c].file}</td>
                    <td>${data[c].spamProbability}</td>
                    <td>${data[c].actualClass}</td>
                    </tr>`;
    console.log(c + " out of 2800");
    document.getElementById("chart").innerHTML += tr;
  }

}

let callURL = "http://localhost:8080/spamDetector-1.0/api/spam";

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


