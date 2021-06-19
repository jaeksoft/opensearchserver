/*
 * Copyright 2017-2021 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import {gql} from "@apollo/client/core";
import {useState} from "react";
import {useMutation, useQuery} from "@apollo/client";
import {
  Box,
  Button, CircularProgress,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField
} from "@material-ui/core";
import {useDispatch} from "react-redux";
import {editSchema} from "./store";

const INDEX_LIST = gql`
  query IndexList($keywords: String, $start: Int, $rows: Int) {
    indexList(keywords: $keywords, start: $start, rows: $rows) {
      name
      id
    }
  }
`;

const CREATE_INDEX = gql`
  mutation Createindex($name: String!) {
    createIndex(name: $name)
  }
`
const DELETE_INDEX = gql`
  mutation Deleteindex($name: String!) {
    deleteIndex(name: $name)
  }
`

interface Index {
  id: string;
  name: string;
}

interface IndexData {
  indexList: Index[];
}

const Indices = () => {
  const dispatch = useDispatch();
  const [keywords, setKeywords] = useState<String>('');
  const [start] = useState<Number>(0);
  const [rows] = useState<Number>(1000);
  const {loading, error, data, refetch} = useQuery<IndexData>(INDEX_LIST, {
    variables: {keywords: keywords, start: start, rows: rows},
    fetchPolicy: "no-cache"
  });
  const [gqlCreate, {loading: loadingCreate}] = useMutation(CREATE_INDEX, {
    variables: {name: keywords},
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  const [gqlDelete, {loading: loadingDelete}] = useMutation(DELETE_INDEX, {
    variables: {name: keywords},
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  if (error) {
    alert(error.message);
    console.error("GQL INDEX_LIST error: ", error);
  }
  return (
    <>
      <Box p={2}>
        <Grid container spacing={1} alignItems={"flex-end"}>
          <Grid item xs>
            <TextField label="Index name" value={keywords} size={"small"} fullWidth={true}
                       onChange={e => setKeywords(e.target.value)}/>
          </Grid>
          <Grid item xs={"auto"}>
            {(loadingCreate || loading) && <CircularProgress size={30}/>}
            {loadingDelete && <CircularProgress size={30} color={"secondary"}/>}
          </Grid>
          <Grid item xs={"auto"}>
            <Button disabled={!keywords || keywords.length === 0 || !data || data.indexList.length > 0}
                    fullWidth={true} size={"small"}
                    variant="contained" onClick={() => gqlCreate().then(() => refetch())}
                    color="primary">Create index
            </Button>
          </Grid>
          <Grid item xs={"auto"}>
            <Button disabled={!data || data.indexList.length !== 1 || data.indexList[0].name !== keywords}
                    fullWidth={true} size={"small"}
                    variant="contained" onClick={() => gqlDelete().then(() => refetch())}
                    color="secondary">Delete index
            </Button>
          </Grid>
        </Grid>
      </Box>
      <TableContainer component={Box}>
        <Table size={"small"}>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Uuid</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data?.indexList.map((item) => (
              <TableRow key={item.id} onClick={() => {
                dispatch(editSchema(item.name))
              }}>
                <TableCell component="th" scope="row">
                  {item.name}
                </TableCell>
                <TableCell>{item.id}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}

export default Indices;
