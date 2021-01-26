<!DOCTYPE html>
<html>
<head>
	<title>Example website</title>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
</head>
<body>
	<div id="output">
		<p><b>Key: </b><span id="outkey">null</span></p>
		<p><b>Value: </b><span id="outvalue">null</span></p>
	</div>
	<div>
		<input type="text" id="key" /><br />
		<input type="text" id="value"><br />
		<button onclick="get()">GET</button>
		<button onclick="put()">PUT</button>
		<button onclick="del()">DELETE</button>
	</div>

	<script type="text/javascript">
		function output(e){

			document.getElementById("outkey").innerHTML = e.key;
			document.getElementById("outvalue").innerHTML = e.data;
		}
		function getValue(){
			var value = document.getElementById('key').value;
			return value;
		}
		function get(){
			$.ajax({
			    type: 'GET', // Use POST with X-HTTP-Method-Override or a straight PUT if appropriate.
			    dataType: 'json', // Set datatype - affects Accept header
			    url: "http://localhost:8080/"+getValue(), // A valid UR 
			    success: function(data){
			    	output(data);
			    }
			});
		}

		function put(){
			$.ajax({
			    type: 'PUT', // Use POST with X-HTTP-Method-Override or a straight PUT if appropriate.
			    dataType: 'json', // Set datatype - affects Accept header
			    url: "http://localhost:8080/"+getValue(), // A valid URL
			    data: {
			    	'data': document.getElementById("value").value
			    },
			    success: function(data){
			    	output(data);
			    }
			});
		}

		function del(){
			$.ajax({
			    type: 'DELETE', // Use POST with X-HTTP-Method-Override or a straight PUT if appropriate.
			    dataType: 'json', // Set datatype - affects Accept header
			    url: "http://localhost:8080/"+getValue(), // A valid URL
			    data: '{"data": "loser"}',
			    success: function(data){
			    	output(data);
			    }
			});
		}
	</script>

</body>
</html>