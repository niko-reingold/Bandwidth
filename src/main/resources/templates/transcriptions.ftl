<br><br>
<ul>
#foreach( $transcription in $transcriptions )
# $velocityCount: $transcription
    <br>
#end
</ul>
<br>

#foreach ( $mediaLink in $mediaLinks )
    <form action='$mediaLink'>
        <input type="submit" value="Play # $velocityCount">
    </form>
#end

<br>

<form action='/'>
  <input type="submit" value="Call/Text">
</form>