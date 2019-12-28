exports.test_str = function (){

    let assert  =require("assert")
    let name = 'NN';

    assert.equal('My name is NN.',`My name is ${name}.`)
    assert.equal('10',`${getAge()}`)

    let hello = `hello world!`;
    assert.equal(hello.startsWith('world',6),true);
    assert.equal(hello.endsWith('hello',5),true);
    assert.equal(hello.includes('hello',6),false);

    



}
function getAge(){
    return "10"
}


