$(document).ready(function() {

 $('#sendEmail').click(function(event) {
     var qrCodeSrc = $('#qrCodeImage').attr('src');
     var qrCodeData = qrCodeSrc.replace('data:image/png;base64,', '');
     var email = document.getElementById("email").value;

    const formData = new FormData();
    formData.append("qrCodeData", qrCodeData);

    $.ajax({
                url: '/api/auth/send-email',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(Object.fromEntries(formData.entries())),
                processData: false,
                success: function() {
                      $('#sendEmail').prop('disabled', true);
                      $('#sendEmail').css('background-color', 'grey');
                      $('#sendEmail').val("Email sent");
                },
    });
});


$.ajax({
                url: '/api/auth/get-activation-data',
                type: 'POST',
                contentType: 'application/json',
                processData: false,
                success: function(response) {
                      $('#qrCodeImage').attr('src', 'data:image/png;base64,' + response.qrCode);
                      $('#qrCodeContainer').css("display","block");

                       $('#activate2fa').click(function(event) {
                                      $.ajax({
                                       url: '/api/auth/activate',
                                       type: 'POST',
                                       contentType: 'application/json',
                                       processData: false,
                                          success: function() {
                                              window.location.href = '/api/auth/login?message=' + encodeURIComponent("Account activated.");
                                              },
                                          error: function() {
                                               window.location.href = '/api/auth/login?message=' + encodeURIComponent("Error when activating account. Try logging in and activating it again.");
                                          }
                                       });

                                   });
                              }
                });
    });