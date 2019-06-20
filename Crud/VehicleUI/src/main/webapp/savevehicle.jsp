<!DOCTYPE html>
<html lang="en">
<head>
<title>Bootstrap Example</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script type="text/javascript">
		
	    $( document ).ready(function() {
	    	$("#onSubmit").click(function() {
		        //alert("buy clicking button")
		        var vehicledata = {
	                "vehiclename": $('#vehiclename').val(),
	                "manfaturingSite": $("#manfaturingSite").val(),
	                "alias": $("#alias").val(),
	                "status": $("#status").val()
	               };
		        
		         var url =  "http://localhost:8080/VehicleUI/test/saveVehicle";
		       //  alert(url)
           
		         $.ajax({
                  url:	url,
                  data:   JSON.stringify(vehicledata),
                  type:	'POST',
                  dataType: 'json',
                  contentType: 'application/json',
                  success:	function(data) {
                    alert('Export Template Created');
                  }
                }); 
		    });
	    });
	    
  </script>

</head>
<body>
	<div class="container">
		<h2>vehicles form</h2>
		<form>
			<div class="form-group">
				<label for="Vehicle">VehicleName:</label> <input type="text"
					class="form-control" id="vehiclename"
					placeholder="Enter vehcile name" name="vehicleName">
			</div>
			<div class="form-group">
				<label for="Site">Manufactured location</label> <input type="text"
					class="form-control" id="manfaturingSite"
					placeholder="Enter vehcile name" name="vehicleName">
			</div>
			<div class="form-group">
				<label for="Alias">Alias:</label> <input type="text"
					class="form-control" id="alias" placeholder="Enter vehcile name"
					name="vehicleName">
			</div>
			<div class="form-group">
				<label for="status">Status:</label> <input type="text"
					class="form-control" id="status" placeholder="Enter vehcile name"
					name="vehicleName">
			</div>
			<button type="submit" id="onSubmit" class="btn btn-default">Submit</button>
		</form>
	</div>

</body>
</html>
