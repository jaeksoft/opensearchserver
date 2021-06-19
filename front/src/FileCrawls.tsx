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
  Box,
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
import {editFileCrawl, editWebCrawl} from "./store";

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

const ABORT_FILE_CRAWL = gql`
  mutation StopFileCrawl($name: String!, $reason: String) {
    abortFileCrawl(name: $name, aborting_reason: $reason)
  }
`

const DELETE_FILE_CRAWL = gql`
  mutation DeleteFileCrawl($name: String!) {
    deleteFileCrawl(name: $name)
  }
`

interface FileCrawlStatus {
  startTime?: number
  endTime?: number
  crawled?: number
  rejected?: number
  error?: number
  running?: boolean
  aborting?: boolean
  abortingReason?: string
}

interface FileCrawlNameStatus {
  name: string
  status: FileCrawlStatus
}

interface FileCrawlData {
  fileCrawlList: FileCrawlNameStatus[];
}

interface FileCrawlRowProps {
  item: FileCrawlNameStatus
  stopCallback: () => void
}

const FileCrawlRow = ({item, stopCallback}: FileCrawlRowProps) => {
  const [gqlAbort, {loading: loadingStop}] = useMutation(ABORT_FILE_CRAWL, {
    onCompleted: data => {
      stopCallback();
    },
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  const [abortingReason, setAbortingReason] = useState<string | undefined>(item.status.abortingReason);

  const abortCrawlAction = useCallback(async () => {
    await gqlAbort({variables: {name: item.name, reason: abortingReason}});
  }, [abortingReason]);

  const toDateTime = (timeMs?: number): string | undefined => {
    if (!timeMs)
      return undefined;
    const date = new Date(timeMs);
    return date.toLocaleString();
  };

  const disableStop = item.status.endTime ? true : item.status.aborting;
  const endTime = item.status.endTime ? toDateTime(item.status.endTime) : item.status.aborting ? 'Aborting...' : undefined;

  return (
    <TableRow key={item.name}>
      <TableCell>
        {item.name}
      </TableCell>
      <TableCell>
        {toDateTime(item.status.startTime)}
      </TableCell>
      <TableCell>
        {endTime}
      </TableCell>
      <TableCell>
        {item.status.crawled}
      </TableCell>
      <TableCell>
        {item.status.rejected}
      </TableCell>
      <TableCell>
        {item.status.error}
      </TableCell>
      <TableCell>
        <Box display={"flex"} alignItems={"flex-end"}>
          <Box mr={1} flexGrow={1}>
            <TextField label="Aborting reason" value={abortingReason} disabled={disableStop}
                       onChange={e => setAbortingReason(e.target.value)} size={"small"} fullWidth={true}/>
          </Box>
          <Button disabled={disableStop} size={"small"}
                  variant="contained" onClick={abortCrawlAction} color="secondary">Stop
          </Button>
        </Box>
      </TableCell>
    </TableRow>
  )
}

const FileCrawls = () => {
  const dispatch = useDispatch();
  const [keywords, setKeywords] = useState<string>('');
  const [start, setStart] = useState<number>(0);
  const [rows, setRows] = useState<number>(1000);
  const {loading, error, data, refetch} = useQuery<FileCrawlData>(FILE_CRAWL_LIST, {
    variables: {keywords: keywords, start: start, rows: rows},
    fetchPolicy: "no-cache",
    pollInterval: 10000
  });
  const [gqlDelete, {loading: loadingDelete, error: errorDelete}] = useMutation(DELETE_FILE_CRAWL, {
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

  const startCrawlAction = useCallback(() => {
    dispatch(editFileCrawl(keywords));
  }, [keywords]);

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
                          variant="contained" onClick={startCrawlAction}
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
            <FileCrawlRow item={item} stopCallback={() => refetch()}/>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default FileCrawls;
