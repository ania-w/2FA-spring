$(document).ready(function() {

 $('#sendEmail').click(function(event) {
    var email = document.getElementById("email").value;
    var username = document.getElementById("username").value;

    const formData = new FormData();
    formData.append("email", email);
    formData.append("username", username);

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

  $('#registerForm').submit(function(event) {
    event.preventDefault();

    var message = document.getElementById("error-register");

    var username = document.getElementById("username").value;
    var email = document.getElementById("email").value;
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;
    var authenticationMethod = document.querySelector('input[name="twoFactorMethod"]:checked').value;

    if (password !== confirmPassword) {
        message.innerHTML = "Passwords do not match!";
        return;
    }

    const formData = new FormData();
    formData.append("username", username);
    formData.append("password", password);
    formData.append("email", email);
    formData.append("twoFactorMethod", authenticationMethod);

    if(authenticationMethod=="NONE"){
        console.log("none");


    $.ajax({
          url: '/api/auth/register-user',
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(Object.fromEntries(formData.entries())),
          processData: false,
          success: function() {
              var message = "User account created successfully."
              window.location.href = '/api/auth/login?message=' + encodeURIComponent(message);
          },
          error: function() {
            alert('Error when creating new account');
          }
        });

    } else if(authenticationMethod=="OTP"){
    console.log("otp");

               $.ajax({
                     url: '/api/auth/register-user',
                     type: 'POST',
                     contentType: 'application/json',
                     data: JSON.stringify(Object.fromEntries(formData.entries())),
                     processData: false,
                     success: function(response) {
                                 var qrCodeData = response.qrCode;
                                 var message = "User account created successfully."
                                 $('#qrCodeImage').attr('src', 'data:image/png;base64,' + qrCodeData);
                                 $('#qrCodeContainer').css("display","block");
                                 $('#registerForm').css("display","none");
                                 $('#activate2fa').click(function(event) {
                                     const form = new FormData();
                                     form.append("username", username);
                                     form.append("email", email);

                                    $.ajax({
                                                         url: '/api/auth/enable-2fa',
                                                         type: 'POST',
                                                         contentType: 'application/json',
                                                         data: JSON.stringify(Object.fromEntries(form.entries())),
                                                         processData: false,
                                                         success: function(response) {
                                                             window.location.href = '/api/auth/login?message=' + encodeURIComponent(message);
                                                         },
                                                         error: function() {
                                                           window.location.href = '/api/auth/login?message=' + encodeURIComponent("Error when activating two factor authentication. Try logging in and activating it manually.");
                                                         }
                                                       });

                                   });
                     },
                     error: function() {
                       alert('Error when creating new account');
                     }
                   });

               }

  });
});
