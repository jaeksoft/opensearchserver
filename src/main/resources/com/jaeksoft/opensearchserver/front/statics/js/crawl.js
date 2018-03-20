/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

$("#inclusion-plus").click(function () {
    var value = $("#inclusion").val();
    $("#inclusion-list").append(crawl.li('inclusion', value));
});

$("#exclusion-plus").click(function () {
    var value = $("#exclusion").val();
    $("#exclusion-list").append(crawl.li('exclusion', value));
});

$("#param-filter-plus").click(function () {
    var value = $("#param-filter").val();
    $("#param-filter-list").append(crawl.li('param-filter', value));
});

$("#path-filter-plus").click(function () {
    var value = $("#path-filter").val();
    $("#path-filter-list").append(crawl.li('path-filter', value));
});

$("#cookie-plus").click(function () {
    var name = $("#cookie-name").val();
    var value = $("#cookie-value").val();
    $("#cookie-list").append(crawl.cookie(name, value));
});

$("#inclusion-list").on('click', '.inclusion-minus', function () {
    $(this).closest('div.form-group').remove()
});

$("#exclusion-list").on('click', '.exclusion-minus', function () {
    $(this).closest('div.form-group').remove()
});

$("#param-filter-list").on('click', '.param-filter-minus', function () {
    $(this).closest('div.form-group').remove()
});

$("#path-filter-list").on('click', '.path-filter-minus', function () {
    $(this).closest('div.form-group').remove()
});

$("#cookie-list").on('click', '.cookie-minus', function () {
    $(this).closest('div.row').remove()
});

var crawl = {

    li: function (type, value) {
        return '<div class="form-group"><div class="input-group mb-3">'
            + '<input class="form-control" readonly name="' + type + '" type="url" value="' + this.encode(value) + '">'
            + '<div class="input-group-append">'
            + '<button class="' + type + '-minus btn btn-outline-secondary" type="button">'
            + '<span class="oi oi-minus"></span>'
            + '</button></div></div></div>';
    },

    cookie: function (name, value) {
        return '<div class="row"><div class="form-group col-md-5">'
            + '<input readonly class="form-control" type="text" name="cookie-name" '
            + 'value="' + this.encode(name) + '"></div>'
            + '<div class="form-group col-md-6">'
            + '<input readonly class="form-control" type="text" name="cookie-value" '
            + 'value="' + this.encode(value) + '"></div>'
            + '<div class="form-group col-md-1 text-center">'
            + '<button class="cookie-minus btn btn-default btn-block" type="button">'
            + '<i class="fa fa-minus" aria-hidden="true"></i></button></div></div>';
    },

    encode: function (value) {
        var s = $('<div/>').text(value).html()
        return s.replace(/"/g, "&quot;")
    }

}
