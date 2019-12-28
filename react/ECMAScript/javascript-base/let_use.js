exports.let_use = function let_use(){
        var a =[];
    
    
        for(let i=0;i<10;i++){
        
        a[i] = function (){
            console.log(i);
        }
    }

    for(let i=0;i<3;i++){
        
        a[i]();
    
    
        console.log(i);
    }

}
 

 