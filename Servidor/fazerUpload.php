<?php
$api_version = 1.0;
$start = microtime(true);
$dir = "";

if(isset($_FILES['image']['name']) )
{
    $file_name = time().basename($_FILES['image']['name']);
    $extension = strtolower(pathinfo($file_name,PATHINFO_EXTENSION));
        if ($extension == 'png' || $extension == 'jpg' || $extension == 'jpeg') {
            if($_FILES["image"]["size"] < 4000001){
                $file = $dir.$file_name;
                if( move_uploaded_file($_FILES['image']['tmp_name'], $file) )
                {
                    $arr = array(
                    'status'=>1, 
                    'message'=>"File Uploaded",
                    'file_name'=>$file_name
                    );
                }
                else
                {
                    $arr = array(
                    'status'=>0, 
                    'error'=>"Something Went Wrong Please Retry",
                    'file_name'=>$file_name
                    );
                }
            }
            else
            {
                $arr = array(
                'status'=>0,
                'error'=>"File size cant exceed 4 MB"
                );
            }  
        }
        else
        {
            $arr = array(
            'status'=>0,
            'error'=>"Only .png, .jpg and .jpeg format are accepted"
            );
        }
}
else{
    $arr = array(
    'status'=>1, 
    'message'=>"Please try Post Method"
    );
}

$arr[ 'api' ] = $api_version;
$arr[ 'time' ] = ( microtime(true) - $start );

print_r( json_encode( $arr ) );

?>