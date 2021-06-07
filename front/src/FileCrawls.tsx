/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
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

const FILE_CRAWL_LIST = gql`
  query FileCrawlList($keywords: String, $start: Int, $rows: Int) {
    fileCrawlList(keywords: $keywords, start: $start, rows: $rows) {
      name
      status {
        aborting
        abortingReason
        crawled
        rejected
        redirect
        error
        lastError
        currentCrawl
        currentDepth
        startTime
        endTime
      }
    }
  }
`;

const RUN_FILE_CRAWL = gql`
  mutation RunFileCrawl($name: String!, $settings: FileCrawlSettings!) {
    runFileCrawl(name: $name, settings: $settings) {
      name
    }
  }
`
const DELETE_FILE_CRAWL = gql`
  mutation DeleteFileCrawl($name: String!) {
    deleteFileCrawl(name: $name)
  }
`

interface FileCrawlData {
  fileCrawlList: string[];
}

const FileCrawls = () => {
  const dispatch = useDispatch();
  const [keywords, setKeywords] = useState<String>('');
  const [start, setStart] = useState<Number>(0);
  const [rows, setRows] = useState<Number>(1000);
  const {loading, error, data, refetch} = useQuery<FileCrawlData>(FILE_CRAWL_LIST, {
    variables: {keywords: keywords, start: start, rows: rows},
    fetchPolicy: "no-cache"
  });
  const [gqlRun, {loading: loadingCreate, error: errorCreate}] = useMutation(RUN_FILE_CRAWL, {
    variables: {name: keywords}
  });
  const [gqlDelete, {loading: loadingDelete, error: errorDelete}] = useMutation(DELETE_FILE_CRAWL, {
    variables: {name: keywords}
  });
  if (error) {
    alert(error.message);
    console.error("GQL FILE_CRAWL_LIST error: ", error);
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
                    <TextField label="File crawl name" value={keywords} size={"small"} fullWidth={true}
                               onChange={(e) => setKeywords(e.target.value)}/>
                  </Grid>
                  <Grid item xs={"auto"}>
                    {loadingCreate || loading && <CircularProgress size={30}/>}
                    {loadingDelete && <CircularProgress size={30} color={"secondary"}/>}
                  </Grid>
                  <Grid item xs={"auto"}>
                    <Button disabled={!keywords || keywords.length == 0 || !data || data.fileCrawlList.length > 0}
                            fullWidth={true} size={"small"}
                            variant="contained" onClick={() => gqlRun().then(() => refetch())}
                            color="primary">Create crawl
                    </Button>
                  </Grid>
                  <Grid item xs={"auto"}>
                    <Button disabled={!data || data.fileCrawlList.length != 1 || data.fileCrawlList[0] != keywords}
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
            {data?.fileCrawlList.map((name) => (
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

export default FileCrawls;
