<!doctype html>

<html lang="en">
<head>
  <meta charset="utf-8">

  <title>Sample Webapp</title>
  <meta name="description" content="Sample Webapp">

  <link rel="stylesheet" href="css/styles.css?v=1.0">

</head>

<body>

    <h1>This is a Sample Web application</h1>

    <p>Headers:<p>
    <pre>
<?php
  $headers =  getallheaders();
  foreach($headers as $key=>$val){
    echo $key . ': ' . $val . '<br>';
  }
?>
  </pre>

  <p>Cookies:<p>
  <pre>
<?php
foreach ($_COOKIE as $key=>$val)
{
echo $key.' is '.$val."<br>\n";
}
?>
  </pre>


    <p>Debug info:</p>
    <pre>
        <?php
            print_r($_SERVER);
        ?>
    </pre>


  <script src="js/scripts.js"></script>
</body>
</html>