$('button#cancel').on('click', function (e) {
    e.preventDefault();
    window.history.back();
    return false;
});
$(document).ready(function () {
    $("#checkBoxCheckAll").click(function () {
        $(".checkBoxClass").prop('checked', $(this).prop('checked'));
    });

    $(".checkBoxClass").change(function () {
        if (!$(this).prop("checked")) {
            $("#checkBoxCheckAll").prop("checked", false);
        }
    });
});