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


import {useDispatch, useSelector} from "react-redux";
import {CrawlsViews, setCrawlsView, State} from "./store";
import {
  Box,
  Button,
  Grid,
  Paper,
  TextField,
  Typography
} from "@material-ui/core";
import CrawlFilterItemView from "./components/CrawlFilterItemView";
import {useEffect, useState} from "react";
import {CrawlFilterItem} from "./types";
import {useMutation} from "@apollo/client";
import {gql} from "@apollo/client/core";

const RUN_WEB_CRAWL = gql`
  mutation RunWebCrawl($name: String!, $settings: WebCrawlSettings!) {
    runWebCrawl(name: $name, settings: $settings) {name}
  }
`

const WebCrawl = () => {
  const selectedWebCrawl = useSelector((state: State) => state.selectedWebCrawl)
  const dispatch = useDispatch();
  const [entryUrl, setEntryUrl] = useState<string>('');
  const [maxDepth, setMaxDepth] = useState<number | undefined>(undefined);
  const [crawlName, setCrawlName] = useState<string>(selectedWebCrawl || '');
  const [crawlList, setCrawlList] = useState<CrawlFilterItem[]>([]);
  const [nextItemIndex, setNextItemIndex] = useState<number>(1);

  const [gqlRunWebCrawl, {loading}] = useMutation(RUN_WEB_CRAWL, {
    variables: {name: crawlName, settings: {entryUrl: entryUrl, maxDepth: maxDepth}},
    onCompleted: data => {
      alert(data);
      console.log(data);
    },
    onError: err => {
      alert(err);
      console.error(err);
    }
  });

  const onCancel = () => {
    dispatch(setCrawlsView(CrawlsViews.WEB_CRAWLS));
  }

  const onRun = async () => {
    await gqlRunWebCrawl();
  }

  const onAdd = (item: CrawlFilterItem) => {
    const newItem = {...item, index: nextItemIndex};
    setNextItemIndex(nextItemIndex + 1);
    const newList = [...crawlList, newItem];
    setCrawlList(newList);
  }

  const onSave = (itemToSave: CrawlFilterItem) => {
    const newList = crawlList.map(item => item.index === itemToSave.index ? itemToSave : item);
    setCrawlList(newList);
  }

  const onDelete = (itemToDelete: CrawlFilterItem) => {
    const newList = crawlList.filter(item => item.index !== itemToDelete.index);
    setCrawlList(newList);
  }

  return (
    <Box p={1}>
      <Paper>
        <Box p={2}>
          <Grid container justify={"space-between"} spacing={10}>
            <Grid item>
              <Typography align={"right"} variant={"h5"}>Start a new crawl</Typography>
            </Grid>
            <Grid item>
              <Grid container spacing={2}>
                <Grid item>
                  <Button variant={"contained"} color={"secondary"}
                          onClick={onCancel}>Cancel</Button>
                </Grid>
                <Grid item>
                  <Button variant={"contained"} color={"primary"}
                          onClick={onRun}>Run</Button>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
          <form noValidate autoComplete="off">
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <TextField fullWidth required id="name" label="Name" value={crawlName}
                           disabled={loading}
                           onChange={e => setCrawlName(e.target.value)}
                           defaultValue={crawlName} placeholder={selectedWebCrawl || 'Crawl name'}/>
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth required id="entryUrl" label="Entry URL" value={entryUrl}
                           disabled={loading}
                           onChange={e => setEntryUrl(e.target.value)}
                           placeholder={"https://www.opensearchserver.com"}/>
              </Grid>
              <Grid item xs={6}>
                <TextField fullWidth id="max_depth" label="Max depth" type={"number"}
                           disabled={loading}
                           value={maxDepth} onChange={e => setMaxDepth(+e.target.value)}
                           placeholder={"1"}/>
              </Grid>
              <Grid item xs={6}>
                <TextField fullWidth id="max_number_url" label="Max number of URL" type={"number"}
                           disabled={loading}
                           placeholder={"1000"}/>
              </Grid>
              <Grid item xs={12}>
                <CrawlFilterItemView onAdd={onAdd} disabled={loading}/>
                {crawlList.map((item) =>
                  <CrawlFilterItemView disabled={loading} key={item.index} filter={item} onSave={onSave}
                                       onDelete={onDelete}/>)}
              </Grid>
            </Grid>
          </form>
        </Box>
      </Paper>
    </Box>
  )
}

export default WebCrawl;
