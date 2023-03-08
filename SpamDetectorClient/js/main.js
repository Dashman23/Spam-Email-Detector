// TODO: onload function should retrieve the data needed to populate the UI
function add_record_API(tableID, data) {

  for (c in data){
    let tableRef = document.getElementById(tableID);

    let newRow = tableRef.insertRow(-1); //creates new row (-1 so it adds at the end)

    let fileCell = newRow.insertCell(0); //creates cells in the row
    let probCell = newRow.insertCell(1);
    let classCell = newRow.insertCell(2);

    let fileText = document.createTextNode(data[c].file); //creates values to be displayed in the cell
    let probText = document.createTextNode(data[c].spamProbability);
    let classText = document.createTextNode(data[c].actualClass);

    fileCell.appendChild(fileText); //appends textnodes to each cell
    probCell.appendChild(probText);
    classCell.appendChild(classText);
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


