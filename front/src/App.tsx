import React from 'react';
import './App.css';
import ContextProvider from "./Context";
import Navbar from "./Navbar";
import View from "./View";

function App() {
  return (
    <ContextProvider>
      <Navbar/>
      <View/>
    </ContextProvider>
  );
}

export default App;
