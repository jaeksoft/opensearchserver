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
import {useDispatch, useSelector} from "react-redux";
import {editFileCrawl, State} from "./store";
import FileCrawlRow from "./FileCrawlRow";
import {CrawlNameStatus} from "./types";

const FILE_CRAWL_LIST = gql`
  query FileCrawlList($keywords: String, $start: Int, $rows: Int) {
    fileCrawlList(keywords: $keywords, start: $start, rows: $rows) {
      name
      status {
        running
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

const DELETE_FILE_CRAWL = gql`
  mutation DeleteFileCrawl($name: String!) {
    deleteFileCrawl(name: $name)
  }
`

interface FileCrawlData {
  fileCrawlList: CrawlNameStatus[];
}

const FileCrawls = () => {
  const dispatch = useDispatch();
  const selectedIndex = useSelector((state: State) => state.selectedIndex);
  const [keywords, setKeywords] = useState<string>('');
  const [start] = useState<number>(0);
  const [rows] = useState<number>(1000);
  const {loading, error, data, refetch} = useQuery<FileCrawlData>(FILE_CRAWL_LIST, {
    variables: {keywords: keywords, start: start, rows: rows},
    fetchPolicy: "no-cache",
    pollInterval: 10000
  });
  const [gqlDelete, {loading: loadingDelete}] = useMutation(DELETE_FILE_CRAWL, {
    variables: {name: keywords},
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  if (error) {
    alert(error.message);
    console.error("GQL FILE_CRAWL_LIST error: ", error);
  }

  const editCrawlAction = useCallback(() => {
    dispatch(editFileCrawl(keywords, selectedIndex, undefined));
  }, [dispatch, selectedIndex, keywords]);

  return (
    <TableContainer component={Paper}>
      <Table size={"small"}>
        <TableHead>
          <TableRow>
            <TableCell colSpan={7}>
              <Grid container spacing={1} alignItems={"flex-end"}>
                <Grid item xs>
                  <TextField label="File crawl name" value={keywords} size={"small"} fullWidth={true}
                             onChange={(e) => setKeywords(e.target.value)}/>
                </Grid>
                <Grid item xs={"auto"}>
                  {loading && <CircularProgress size={30}/>}
                  {loadingDelete && <CircularProgress size={30} color={"secondary"}/>}
                </Grid>
                <Grid item xs={"auto"}>
                  <Button disabled={!keywords || keywords.length === 0 || !data || data.fileCrawlList.length > 0}
                          fullWidth={true} size={"small"}
                          variant="contained" onClick={editCrawlAction}
                          color="primary">Create crawl
                  </Button>
                </Grid>
                <Grid item xs={"auto"}>
                  <Button disabled={!data || data.fileCrawlList.length !== 1 || data.fileCrawlList[0].name !== keywords}
                          fullWidth={true} size={"small"}
                          variant="contained" onClick={() => gqlDelete().then(() => refetch())}
                          color="secondary">Delete crawl
                  </Button>
                </Grid>
              </Grid>
            </TableCell>
          </TableRow>
          {data && data.fileCrawlList && data.fileCrawlList.length > 0 && <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Started</TableCell>
            <TableCell>Ended</TableCell>
            <TableCell>Crawled</TableCell>
            <TableCell>Rejected</TableCell>
            <TableCell>Error</TableCell>
            <TableCell></TableCell>
          </TableRow>}
        </TableHead>
        <TableBody>
          {data?.fileCrawlList.map((item) =>
            <FileCrawlRow key={item.name} item={item} completionCallback={() => refetch()}/>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default FileCrawls;
