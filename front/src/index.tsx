import React from 'react';
import ReactDOM from 'react-dom';
import {Provider, useSelector} from "react-redux";
import store, {State, Views} from "./store";
import Navbar from "./Navbar";
import Gql from "./Gql";
import Indices from "./Indices";
import {ApolloClient, ApolloProvider, InMemoryCache} from "@apollo/client";
import {GRAPHQL_ENDPOINT} from "./constants";
import Schema from "./Schema";
import Crawls from "./Crawls";
import {Box} from "@material-ui/core";

const View = () => {
  const view = useSelector<State>(state => state.view)
  switch (view) {
    case Views.INDICES:
      return (
        <Indices/>
      );
    case Views.CRAWLS:
      return (
        <Crawls/>
      );
    case Views.QUERIES:
      return (
        <div>QUERIES</div>
      );
    case Views.GRAPHQL:
      return (
        <Gql/>
      );
    case Views.SCHEMA:
      return (
        <Schema/>
      );
    default:
      return null;
  }
}

const apolloClient = new ApolloClient({
  uri: GRAPHQL_ENDPOINT,
  cache: new InMemoryCache(),
});

ReactDOM.render(
  <React.StrictMode>
    <Provider store={store}>
      <ApolloProvider client={apolloClient}>
        <Box m={0} display={"flex"} flexDirection={"column"} height={"100vh"}>
          <Box m={0} marginBottom={1}>
            <Navbar/>
          </Box>
          <Box m={0} p={0} height={"100vh"}>
            <View/>
          </Box>
        </Box>
      </ApolloProvider>
    </Provider>
  </React.StrictMode>,
  document.getElementById('root')
);
