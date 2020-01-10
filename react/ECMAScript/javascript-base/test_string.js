exports.test_str = function (){

    let assert  =require("assert")
    let name = 'NN';

    assert.equal('My name is NN.',`My name is ${name}.`)
    assert.equal('10',`${getAge()}`)

    let hello = `hello world!`;
    assert.equal(hello.startsWith('world',6),true);
    assert.equal(hello.endsWith('hello',5),true);
    assert.equal(hello.includes('hello',6),false);

    let repeatStr = "n";
    assert.equal(repeatStr.repeat(2),'nn');

    let padStr = "1月";

    assert.equal(padStr.padStart(3,"0"),"01月");

    assert.equal(padStr.padEnd(3,'0'),"1月0");

    let trimStr = " abc ";
    assert.equal(trimStr.trimLeft(),'abc ');
    assert.equal(trimStr.trimRight(),' abc');
    assert.equal(trimStr.trim(),'abc');

    

    



}
function getAge(){
    return "10"
}


