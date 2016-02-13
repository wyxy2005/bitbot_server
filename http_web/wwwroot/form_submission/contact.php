<?php
//$enable_GoogleCaptchaValidation = false;
//$captcha_secretkey = '6LcdKAsTAAAAAHzuQ3QwISCz6tP_TnYtPBVnrTvw';

echo '<meta http-equiv="refresh" content="0; URL=http://newcurrency.co/index.html?status=ok">';
exit;

// Doesnt need for CA2 yet! LOL :D 
/*if (isset($_POST['name']) 
	&& isset($_POST['email']) 
	&& (!$enable_GoogleCaptchaValidation || ($enable_GoogleCaptchaValidation && isset($_POST['g-recaptcha-response'])))  
	&& isset($_POST['desc'])) {

	$name = $_POST['name'];
	$email = $_POST['email'];
	$grecaptcharesponse = $_POST['g-recaptcha-response'];
	$description = $_POST['desc'];

	if (strlen($description) < 20 || 
		!filter_var($email, FILTER_VALIDATE_EMAIL)) { // http://php.net/manual/en/filter.examples.validation.php
		echo ("An unknown error occured.");
	} else {
		if ($enable_GoogleCaptchaValidation) {
			$response = _recaptcha_http_post ("https://www.google.com", "/recaptcha/api/verify",
				array (
		  		'secret' => $captcha_secretkey,
		  		'response' => $grecaptcharesponse));
echo ("LOLS");
			if ($response->success) {
				echo ("Thanks for the feedback!");
			} else {
				echo ("Captcha error! " + $response->error-codes);
			}
		}
	}
} else {
	echo("TEST");
}*/

/**
 * Submits an HTTP POST to a reCAPTCHA server
 * @param string $host
 * @param string $path
 * @param array $data
 * @param int port
 * @return array response
 */
function _recaptcha_http_post($host, $path, $data, $port = 80) {

        $req = _recaptcha_qsencode ($data);

        $http_request  = "POST $path HTTP/1.0\r\n";
        $http_request .= "Host: $host\r\n";
        $http_request .= "Content-Type: application/x-www-form-urlencoded;\r\n";
        $http_request .= "Content-Length: " . strlen($req) . "\r\n";
        $http_request .= "User-Agent: reCAPTCHA/PHP\r\n";
        $http_request .= "\r\n";
        $http_request .= $req;

        $response = '';
        if( false == ( $fs = @fsockopen($host, $port, $errno, $errstr, 10) ) ) {
                die ('Could not open socket');
        }

        fwrite($fs, $http_request);

        while ( !feof($fs) ) {
                $response .= fgets($fs, 1160); // One TCP-IP packet
        }
        fclose($fs);
        $response = explode("\r\n\r\n", $response, 2);

        return $response;
}

?>