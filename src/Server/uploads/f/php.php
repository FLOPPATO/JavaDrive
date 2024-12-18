<!DOCTYPE html>
<html lang="it">
<body>
	<?php
		$servername = "localhost";
		$username = "root";

		// Create connection
		$conn = new mysqli($servername, $username);
		// Check connection
		if ($conn->connect_error) {
		  die("Connection failed: " . $conn->connect_error);
		}
		

		
		$sql = "use floppa;";
		$conn->query($sql);
		
		
		
		switch ($_POST["invia"]){
			case "in":
				$NOME = $_POST["nome"];
				$COGNOME = $_POST["cognome"];
				$id = $_POST["id"];
				if($id === "")
					$sql = "INSERT INTO test (NOME, COGNOME) VALUES ('$NOME', '$COGNOME');";
				else
					$sql = "INSERT INTO test (NOME, COGNOME, VALORE) VALUES ('$NOME', '$COGNOME', $id);";
				try{$try = $conn->query($sql);}
				catch(Exception $e){echo "valore non inserito (sameID)";}
				if(isset($try)) header("location: html.html");
			break;
			case "out":
				$sql = "SELECT * FROM test;";
				$result = $conn->query($sql);
				while($row = $result->fetch_assoc())
					echo "Nome: " . $row['NOME'] . " - Cognome: " . $row['COGNOME'] . " - Valore: " . $row['VALORE'] . "<br>";
			break;
			case "clear":
				$sql = "TRUNCATE TABLE test;";
				$conn->query($sql);
				header("location: html.html");
			break;
			case "advanced":
				header("location: advanced.html");
			break;
			case "cerca":
				$sql = "SELECT * FROM test WHERE VALORE" . (isset($_POST["iddac"])?'>=':'>') . " " . $_POST['idda'] . " and VALORE " . (isset($_POST["idac"])?'<=':'<') . " " . $_POST['ida'] . ";";
				$result = $conn->query($sql);
				while($row = $result->fetch_assoc())
					echo "Nome: " . $row['NOME'] . " - Cognome: " . $row['COGNOME'] . " - Valore: " . $row['VALORE'] . "<br>";
			break;
			
		}

		$conn->close();
	?>
</body>
</html>