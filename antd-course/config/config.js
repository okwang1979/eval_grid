export default {
    singular: true,
    routes: [{
        path: '/',
        component: './HelloWorld',
      }],
      plugins: [
        ['umi-plugin-react', {
          antd: true
        }],
      ],
};