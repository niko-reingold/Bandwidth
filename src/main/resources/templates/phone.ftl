<br><br>
<form method="POST" action='/phone'>
  <div>Number: +1
  <input type="text" name="number" required="true"></div> <br>
  <div>Text:     
  <input type="text" name="words" required="true"></div> <br>

  <div><input type="radio" name="action" value="call" checked="checked"> Call   
  <input type="radio" name="action" value="text"> Text   
  <input type="submit" name="Send" value="Send"></div>
</form>

<br>

<form method="POST" action="/transfer">
	All calls to +18328627643 will be forwarded to +1
	<input type="text" name="forward">
	<input type="submit" name="Update" value="Update">
</form>
