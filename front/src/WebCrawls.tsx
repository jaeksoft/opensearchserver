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
import {useCallback, useState} from "react";
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
import {editSchema, editWebCrawl} from "./store";

const CRAWL_LIST = gql`
  query WebCrawlList($keywords: String, $start: Int, $rows: Int) {
    webCrawlList(keywords: $keywords, start: $start, rows: $rows) {
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

const RUN_CRAWL = gql`
  mutation RunWebCrawl($name: String!, $settings: WebCrawlSettings!) {
    runWebCrawl(name: $name, settings: $settings) {
      name
    }
  }
`

const STOP_CRAWL = gql`
  mutation StopWebCrawl($name: String!, $reason: String) {
    stopWebCrawl(name: $name, aborting_reason: $reason)
  }
`

const DELETE_CRAWL = gql`
  mutation DeleteWebCrawl($name: String!) {
    deleteWebCrawl(name: $name)
  }
`

interface WebCrawlStatus {
  name: string
}

interface WebCrawlData {
  webCrawlList: WebCrawlStatus[];
}

const WebCrawls = () => {
  const dispatch = useDispatch();
  const [keywords, setKeywords] = useState<string>('');
  const [start, setStart] = useState<number>(0);
  const [rows, setRows] = useState<number>(20);
  const {loading, error, data, refetch} = useQuery<WebCrawlData>(CRAWL_LIST, {
    variables: {keywords: keywords, start: start, rows: rows},
    fetchPolicy: "no-cache",
  });
  const [gqlRun, {loading: loadingCreate}] = useMutation(RUN_CRAWL, {
    variables: {name: keywords}
  });
  const [gqlDelete, {loading: loadingDelete}] = useMutation(DELETE_CRAWL, {
    variables: {name: keywords},
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  if (error) {
    alert(error.message);
    console.error("GQL CRAWL_LIST error: ", error);
  }

  console.log("data.webCrawlList", data?.webCrawlList);

  const startCrawlAction = useCallback(() => {
    dispatch(editWebCrawl(keywords));
  }, [keywords]);

  return (
    <TableContainer component={Paper}>
      <Table size={"small"}>
        <TableHead>
          <TableRow>
            <TableCell colSpan={2}>
              <Grid container spacing={1} alignItems={"flex-end"}>
                <Grid item xs>
                  <TextField label="Web crawl name" value={keywords} size={"small"} fullWidth={true}
                             onChange={(e) => setKeywords(e.target.value)}/>
                </Grid>
                <Grid item xs={"auto"}>
                  {loadingCreate || loading && <CircularProgress size={30}/>}
                  {loadingDelete && <CircularProgress size={30} color={"secondary"}/>}
                </Grid>
                <Grid item xs={"auto"}>
                  <Button disabled={!keywords || keywords.length == 0 || !data || data.webCrawlList.length > 0}
                          fullWidth={true} size={"small"}
                          variant="contained" onClick={startCrawlAction}
                          color="primary">Create crawl
                  </Button>
                </Grid>
                <Grid item xs={"auto"}>
                  <Button disabled={!data || data.webCrawlList.length != 1 || data.webCrawlList[0].name != keywords}
                          fullWidth={true} size={"small"}
                          variant="contained" onClick={() => gqlDelete().then(() => refetch())}
                          color="secondary">Delete crawl
                  </Button>
                </Grid>
              </Grid>
            </TableCell>
          </TableRow>
          {data && data.webCrawlList && data.webCrawlList.length > 0 && <TableRow>
            <TableCell>Name</TableCell>
          </TableRow>}
        </TableHead>
        <TableBody>
          {data?.webCrawlList.map((webCrawlStatus) => (
            <TableRow key={webCrawlStatus.name} onClick={() => {
              dispatch(editSchema(webCrawlStatus.name))
            }}>
              <TableCell component="th" scope="row">
                {webCrawlStatus.name}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default WebCrawls;
