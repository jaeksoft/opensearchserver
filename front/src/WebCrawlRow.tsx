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

import {useDispatch} from "react-redux";
import {useLazyQuery, useMutation} from "@apollo/client";
import {useCallback, useState} from "react";
import {editWebCrawl} from "./store";
import {Box, Button, TableCell, TableRow, TextField} from "@material-ui/core";
import {gql} from "@apollo/client/core";
import {WebCrawlSettings} from "./types";

const RUN_WEB_CRAWL = gql`
  mutation RunWebCrawl($name: String!) {
    runWebCrawl(name: $name) {
      name
    }
  }
`
const ABORT_WEB_CRAWL = gql`
  mutation AbortWebCrawl($name: String!, $reason: String) {
    abortWebCrawl(name: $name, aborting_reason: $reason)
  }
`

const GET_WEB_CRAWL = gql`
  query GetWebCrawl($name: String!) {
    getWebCrawl(name: $name) {
      index
      settings {
        entryUrl
        maxDepth
        filters {
          pattern
          status
        }
        filterPolicy
      }
    }
  }
`

interface GetWebCrawl {
  index?: string,
  settings: WebCrawlSettings
}

interface GetWebCrawlData {
  getWebCrawl?: GetWebCrawl;
}

export interface WebCrawlStatus {
  startTime?: number
  endTime?: number
  crawled?: number
  rejected?: number
  error?: number
  running?: boolean
  aborting?: boolean
  abortingReason?: string
}

export interface WebCrawlNameStatus {
  name: string
  status: WebCrawlStatus
}

export interface WebCrawlRowProps {
  item: WebCrawlNameStatus
  completionCallback: () => void
}

const WebCrawlRow = ({item, completionCallback}: WebCrawlRowProps) => {
  const dispatch = useDispatch();
  const [gqlRun] = useMutation(RUN_WEB_CRAWL, {
    onCompleted: data => {
      completionCallback();
    },
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  const [gqlAbort] = useMutation(ABORT_WEB_CRAWL, {
    onCompleted: data => {
      completionCallback();
    },
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  const [gqlGetCrawl] = useLazyQuery<GetWebCrawlData>(GET_WEB_CRAWL, {
    variables: {name: item.name},
    fetchPolicy: 'no-cache',
    onCompleted: data => {
      console.log("gqlGetCrawl", data);
      if (!data.getWebCrawl) {
        const err = "The crawl does not exist: " + item.name;
        alert(err);
        console.error(err);
      } else {
        dispatch(editWebCrawl(item.name, data.getWebCrawl.index, data.getWebCrawl.settings));
      }
    },
    onError: err => {
      alert(err);
      console.error(err);
    }
  });
  const [abortingReason, setAbortingReason] = useState<string>(item.status.abortingReason || '');

  const runCrawlAction = useCallback(async () => {
    await gqlRun({variables: {name: item.name}});
  }, [gqlRun, item.name]);

  const abortCrawlAction = useCallback(async () => {
    await gqlAbort({variables: {name: item.name, reason: abortingReason}});
  }, [gqlAbort, abortingReason, item.name]);

  const editGetCrawlAction = () => {
    console.log("editGetCrawlAction");
    gqlGetCrawl();
  }

  const toDateTime = (timeMs?: number): string | undefined => {
    if (!timeMs)
      return undefined;
    const date = new Date(timeMs);
    return date.toLocaleString();
  };

  const disableAbort = !item.status.running || item.status.aborting;
  const disableRun = item.status.running;
  const endTime = item.status.endTime ? toDateTime(item.status.endTime) : item.status.aborting ? 'Aborting...' : item.status.running ? 'Running...' : undefined;

  return (
    <TableRow key={item.name}>
      <TableCell>
        <Button size={"small"} variant={'outlined'} onClick={editGetCrawlAction}>
          {item.name}
        </Button>
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
            <TextField label="Aborting reason" value={abortingReason} disabled={disableAbort}
                       onChange={e => setAbortingReason(e.target.value)} size={"small"} fullWidth={true}/>
          </Box>
          <Box mr={1}>
            <Button disabled={disableAbort} size={"small"}
                    variant="contained" onClick={abortCrawlAction} color="secondary">Abort
            </Button>
          </Box>
          <Button disabled={disableRun} size={"small"}
                  variant="contained" onClick={runCrawlAction} color="primary">Run
          </Button>
        </Box>
      </TableCell>
    </TableRow>
  )
}

export default WebCrawlRow;
