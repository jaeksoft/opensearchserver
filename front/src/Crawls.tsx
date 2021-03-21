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
  Button, CircularProgress,
  Grid,
  Paper,
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

const CRAWL_LIST = gql`
  query CrawlList($keywords: String, $start: Int, $rows: Int) {
    crawlList(keywords: $keywords, start: $start, rows: $rows)
  }
`;

const CREATE_CRAWL = gql`
  mutation CreateCrawl($name: String!) {
    createCrawl(name: $name)
  }
`
const DELETE_CRAWL = gql`
  mutation DeleteCrawl($name: String!) {
    deleteCrawl(name: $name)
  }
`

interface CrawlData {
  crawlList: string[];
}

const Crawls = () => {
  const dispatch = useDispatch();
  const [keywords, setKeywords] = useState<String>('');
  const [start, setStart] = useState<Number>(0);
  const [rows, setRows] = useState<Number>(20);
  const {loading, error, data, refetch} = useQuery<CrawlData>(CRAWL_LIST, {
    variables: {keywords: keywords, start: start, rows: rows},
    fetchPolicy: "no-cache"
  });
  const [gqlCreate, {loading: loadingCreate, error: errorCreate}] = useMutation(CREATE_CRAWL, {
    variables: {name: keywords}
  });
  const [gqlDelete, {loading: loadingDelete, error: errorDelete}] = useMutation(DELETE_CRAWL, {
    variables: {name: keywords}
  });
  if (error) {
    alert(error.message);
    console.error("GQL CRAWL_LIST error: ", error);
  }
  return (
    <>
      <TableContainer component={Paper}>
        <Table size={"small"}>
          <TableHead>
            <TableRow>
              <TableCell colSpan={2}>
                <Grid container spacing={1} alignItems={"flex-end"}>
                  <Grid item xs>
                    <TextField label="Crawl name" value={keywords} size={"small"} fullWidth={true}
                               onChange={(e) => setKeywords(e.target.value)}/>
                  </Grid>
                  <Grid item xs={"auto"}>
                    {loadingCreate || loading && <CircularProgress size={30}/>}
                    {loadingDelete && <CircularProgress size={30} color={"secondary"}/>}
                  </Grid>
                  <Grid item xs={"auto"}>
                    <Button disabled={!keywords || keywords.length == 0 || !data || data.crawlList.length > 0}
                            fullWidth={true} size={"small"}
                            variant="contained" onClick={() => gqlCreate().then(() => refetch())}
                            color="primary">Create crawl
                    </Button>
                  </Grid>
                  <Grid item xs={"auto"}>
                    <Button disabled={!data || data.crawlList.length != 1 || data.crawlList[0] != keywords}
                            fullWidth={true} size={"small"}
                            variant="contained" onClick={() => gqlDelete().then(() => refetch())}
                            color="secondary">Delete crawl
                    </Button>
                  </Grid>
                </Grid>
              </TableCell>
            </TableRow>
            <TableRow>
              <TableCell>Name</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data?.crawlList.map((name) => (
              <TableRow key={name} onClick={() => {
                dispatch(editSchema(name))
              }}>
                <TableCell component="th" scope="row">
                  {name}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}

export default Crawls;
