//结构测试

function addNum(num){
    return num+1;
}


exports.dest = function  destructuring(){
    let assert = require('assert');
    let [a,b,c] = ["aa",3,"cc"];

    assert.equal(a,'aa');
    assert.equal(b,3);
    assert.equal(c,'cc');
    let  setValue = new Set();
    setValue.add(1);setValue.add(2);
    setValue.add(3);

    let [x, y, z,q='e',...n] = new Set(['a', 'b', 'c']);
    assert.equal(x,'a');
    assert.equal(y,'b');
    assert.equal(z,'c');
    assert.equal(q,'e');
    assert.equal(n.length,0);

    let values = [1,2].map((a)=>a+1);

    assert.equal(values[0],2);

    let { foo, bar } = { foo:{dd:'aaa',name:'aa'} , bar: 'bbb' };

    let [xx,yy] =[1,2];
    [xx,yy] = [yy,xx];
    assert.equal(xx,2);

    

    assert.equal(foo.dd,"aaa");


    let jsonData = {
        id: 42,
        status: "OK",
        data: [867, 5309]
      };
      
      let { id, status, data:number } = jsonData;
      
      assert.equal(id,42);
      assert.equal(status,"OK");
      assert.equal(number.length,2);
      
    const map = new Map();
    map.set("one","hellow");
    map.set("two","world");

    for(let[key,value] of map){
        if(key=="one"||key=="two"){

        }else{
            assert.equal(true,false);
        }
        
    }


    
}