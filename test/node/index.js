var http = require('http');


async function send(method){

	var random = Math.random(0, 999);

	var options = {
	  host: 'localhost',
	  port: 8080,
	  path: '/testkey',
	  method: method
	};

	var req = http.request(options, function(res) {
	  console.log('STATUS: ' + res.statusCode);
	  console.log('HEADERS: ' + JSON.stringify(res.headers));
	  res.setEncoding('utf8');
	  res.on('data', function (chunk) {
	    console.log('BODY: ' + chunk);
	  });
	  console.log("========================"+method+"=======================")
	});

	req.on('error', function(e) {
	  console.log('problem with request: ' + e.message);
	});

	// write data to request body
	req.write('data\n');
	req.write('data\n');
	req.end();

}



send("PUT");