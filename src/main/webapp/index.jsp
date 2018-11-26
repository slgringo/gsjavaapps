<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Parts</title>
    <script type="text/javascript" src="js/parts.js"></script>
    <link href="css/style.css" rel="stylesheet" type="text/css"></link>
</head>
<body class="pageClass" onload="doFilter()">
    <div class="mainLayout">
        <div class="filterHeader">
            <td>Filter</td>
        </div>
    <table id="filters">
        <tr>
            <td>PN</td>
            <td><input type="text" id="iPn" title="Part number"></td>
            <td></td>
        </tr>
        <tr>
            <td>Part Name</td>
            <td><input type="text" id="iPname" title="Part name"></td>
            <td></td>
        </tr>
        <tr>
            <td>Vendor</td>
            <td><input type="text" id="iVendor" title="Vendor"></td>
            <td></td>
        </tr>
        <tr>
            <td>Qty</td>
            <td><input type="text" id="iQty" title="Quantity"></td>
            <td></td>
        </tr>
        <tr>
            <td>Shipped</td>
            <td><input type="text" id="shippedFrom"><input type="text" id="shippedTo"></td>
            <td></td>
        </tr>
        <tr>
            <td>Received</td>
            <td><input type="text" id="receivedFrom"><input type="text" id="receivedTo"></td>
            <td></td>
        </tr>
    </table>
        <div onclick="doFilter()" class="clButton">Filter</div>
    <div id="testDiv"></div>
    <div class="datatableContainer" id="dataTableDiv">
        <table id="data" class="datatable" width="100%">
            <thead>
                <tr>
                    <th id="part_number" onclick="columnClick(this)">PN<div id="part_number_im" class="arrow"></div></th>
                    <th id="part_name" onclick="columnClick(this)">Part Name<div id="part_name_im" class="arrow"></div></th>
                    <th id="vendor" onclick="columnClick(this)">Vendor<div id="vendor_im" class="arrow"></div></th>
                    <th id="qty" onclick="columnClick(this)">Qty<div id="qty_im" class="arrow"></div></th>
                    <th id="shipped" onclick="columnClick(this)">Shipped<div id="shipped_im" class="arrow"></div></th>
                    <th id="receive" onclick="columnClick(this)">Received<div id="receive_im" class="arrow"></div></th>
                </tr>
            </thead>
            <tbody onscroll="loadNext()">

            </tbody>
        </table>
    </div>
    </div>
</body>
</html>