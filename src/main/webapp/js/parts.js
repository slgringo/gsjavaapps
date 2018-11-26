/**
 * Created by Orefin on 22.11.18.
 *
 */

var columns = ["part_number", "part_name", "vendor", "qty", "shipped", "receive"]; //table columns
var sortColumn = ''; //column for sorting
var sortDirection = "NONE";
var lastRow = 0; //last loaded row from result set
var lastRowIndex;
var reload = true; //reload all data flag
var pageSize = 300; //result set page size

//validation for INT filter fields
function validateInt(text, fieldName) {
    var intRe = new RegExp('^(\\d*)$');
    if (!intRe.test(text)) {
        alert(fieldName + ' field should contains digits only ');
        return false;
    }
    return true;
}

//validation for DATE filter fields
function validateDate(text, fieldName) {
    var intRe = new RegExp('^(((JAN)|(FEB)|(MAR)|(APR)|(MAY)|(JUN)|(JUL)|(AUG)|(SEP)|(OKT)|(NOV)|(DEC)) [0-3]{0,1}\\d, \\d{4}){0,1}$');
    if (!intRe.test(text)) {
        alert(fieldName + ' field should contains date in format  MMM dd, yyyy');
        return false;
    }
    return true;
}

//validation of form fields
function validateFields(qty, shippedFrom, shippedTo, receivedFrom, receivedTo) {
    return validateInt(qty.value, 'Qty') && validateDate(shippedFrom.value, "Shipped From") && validateDate(shippedTo.value, "Shipped To")
           && validateDate(receivedFrom.value, "Received From") && validateDate(receivedTo.value, "Received To");
}

//creating filter params string
function createFilterParams(pN, pName, vendor, qty, shippedFrom, shippedTo, receivedFrom, receivedTo) {
    var s = '"filter":{';
    if (pN.value)
        s += ('"[like]part_number":"' + pN.value + '", ');
    if (pName.value)
        s += ('"[like]part_name":"' + pName.value + '", ');
    if (vendor.value)
        s += ('"[like]vendor":"' + vendor.value + '", ');
    if (qty.value)
        s += ('"[moreeq]qty":"' + qty.value + '", ');
    if (shippedFrom.value)
        s += ('"[moreeq]shipped":"' + shippedFrom.value + '", ');
    if (shippedTo.value)
        s += ('"[lesseq]shipped":"' + shippedTo.value + '", ');
    if (receivedFrom.value)
        s += ('"[moreeq]receive":"' + receivedFrom.value + '", ');
    if (receivedTo.value)
        s += ('"[lesseq]receive":"' + receivedTo.value + '", ');
    if (s !== '"filter":{')
        s = s.slice(0, -1);
    s += '}';
    return s;
}

//creating sort params string
function createSortParams() {
   if (sortDirection !== 'NONE') {
       return ', "sorting":"[' + sortDirection + ']' + sortColumn + '"';
   }
   return '';
}

//render table with data loaded from DB (on filter apply)
function printResponse(responseText) {
    var json = JSON.parse(responseText);
    if (json.hasOwnProperty('error')) {
        document.getElementById('testDiv').innerHTML = json['error'];
    } else {
        var table = document.getElementById('data').getElementsByTagName('tbody')[0];
        table.innerHTML = '';
        printRows(table, json, 0);
    }
}

//paging params string
function addPagingParams() {
    return ', "page":{"LIMIT":"' + pageSize + '", "OFFSET":"' + (reload ? 0 : (lastRowIndex + 1)) + '"}';
}

//filter function
function doFilter() {
    var pn = document.getElementById('iPn');
    var pName = document.getElementById('iPname');
    var vendor = document.getElementById('iVendor');
    var qty = document.getElementById('iQty');
    var shippedFrom = document.getElementById('shippedFrom');
    var shippedTo = document.getElementById('shippedTo');
    var receivedFrom = document.getElementById('receivedFrom');
    var receivedTo = document.getElementById('receivedTo');
    if (!validateFields(qty, shippedFrom, shippedTo, receivedFrom, receivedTo)) {
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.open('POST', './serve', false);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    var body = '{' + createFilterParams(pn, pName, vendor, qty, shippedFrom, shippedTo, receivedFrom, receivedTo)
        + ', "table":"part_entity"' + createSortParams() + addPagingParams() + '}';
    xhr.send(body);
    if (xhr.status != 200) {
        alert('Something went wrong. No response received');
    } else {
        if (reload) {
            printResponse(xhr.responseText);
        } else {
            addNewRows(xhr.responseText);
        }
    }
}

//click handler for data table column headers (sorting)
function columnClick(element) {
    if (element.id !== sortColumn) {
        var imDiv = document.getElementById(sortColumn + '_im');
        if (imDiv)
            imDiv.innerHTML = '';
        sortColumn = element.id;
        sortDirection = 'ASC';
    } else {
        switch (sortDirection) {
            case 'NONE':
                sortDirection = 'ASC';
                break;
            case 'ASC':
                sortDirection = 'DESC';
                break;
            case 'DESC':
                sortDirection = 'NONE';
                break;
        }
    }
    var imgDiv = document.getElementById(element.id + '_im');
    switch (sortDirection) {
        case 'NONE':
            imgDiv.innerHTML = '';
            break;
        case 'ASC':
            imgDiv.innerHTML = '<img src="img/arrow_down.png" width="15px">';
            break;
        case 'DESC':
            imgDiv.innerHTML = '<img src="img/arrow_up.png" width="15px">';
            break;
    }
    doFilter();
}

//load next page from DB on scroll
function loadNext() {
    var table = document.getElementById('dataTableDiv');
    var tableRect = table.getBoundingClientRect();
    var lastRowRect = lastRow.getBoundingClientRect();
    if (tableRect.bottom >= lastRowRect.bottom) {
        if (reload) {
            reload = false;
            doFilter();
        }
    }
}

//add new rows to table on scroll
function addNewRows(responseText) {
    var json = JSON.parse(responseText);
    var table = document.getElementById('data').getElementsByTagName('tbody')[0];
    printRows(table, json, lastRowIndex + 1);
    reload = true;

}

//render table rows from received JSON
function  printRows(table, json, offset) {
    for (var i = 0; i < json.length; i++) {
        var row = table.insertRow(i + offset);
        lastRow = row;
        lastRowIndex = i + offset;
        var j = 0;
        for (var val in columns) {
            row.insertCell(j);
            var value = json[i][columns[val]];
            if (value !== undefined)
                table.rows[i + offset].cells[j].innerHTML = value;
            j++;
        }
    }
}
