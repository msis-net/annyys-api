/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var maininfo= {
    "name": "ABCクリニック", 
    "address":"東京都世田谷区奥沢6-25-1",
    "tel":"03-1234-5678",
    "mail":"abc@annyys.net"
};

function setParameta(value){
    var PARA= new Array();
    var para =value.split("&");
    for(var i in para){
    	var tempval=para[i].split("=");
	var key=tempval[0];
	var val=tempval[1];
	//alert(key+"="+val);
	PARA[key]=val;
    }
    return PARA;
}

function setRestGet(url){
    var return_value;
    $.ajax({
        url:url,
        async: false,
        type:'get',
        dataType:'text',
        timeout:1000,
        success: function(data){
           return_value=data;
        }
    });
    return return_value;
}

function setRestPost(url,data){
    var return_value;
    $.ajax({
        url:url,
        type:'post',
        async: false,
        data: data,
        dataType:'text',
        timeout:2000,
        success: function(data){
           return_value=data;
        }
    });
    return return_value;
}

// -------------------------------------------------
// XML読み込み
// -------------------------------------------------


function xmlLoad(url){
    $.ajax({
        url:url,
        async: false,
        type:'get',
        dataType:'xml',
        timeout:1000,
        success:parse_xml
    });
}

// -------------------------------------------------
// XMLデータを取得
// -------------------------------------------------

function parse_xml(xml,status){
    if(status!='success')return;
    $(xml).find('record').each(disp);
}
// -------------------------------------------------
// HTML生成関数
// -------------------------------------------------
function disp(){
    //各要素を変数に格納
    var $no = $(this).attr('no');
    var $id = $(this).attr('id');
    var $spantime = $(this).attr('spantime');
    var $addtime = $(this).attr('addtime');
    var $status = $(this).attr('status');
    //HTMLを生成
    if($status.match(/[0-2]/)){
    
   
    }
}



// HTTP通信用
function createXMLHttpRequest(cbFunc){
var XMLhttpObject = null;
    try{
	XMLhttpObject = new XMLHttpRequest();
    }catch(e){
        try{
            XMLhttpObject = new ActiveXObject("MSXML2.XMLHTTP.6.0");
        }catch(e){
            try{
                XMLhttpObject = new ActiveXObject("MSXML2.XMLHTTP.3.0");
            }catch(e){
                try {
                    XMLhttpObject = new ActiveXObject('MSXML2.XMLHTTP');
                } catch (e) {
                }
            }
        }
    }
    if (XMLhttpObject) XMLhttpObject.onreadystatechange = cbFunc;
        return XMLhttpObject;
    }
 	
 	function httpAbort(){//処理の停止用
		httpObj.abort();
	}




