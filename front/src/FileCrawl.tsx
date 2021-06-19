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
import {useState} from "react";
import {CrawlFilterItem} from "./types";
import {useMutation} from "@apollo/client";
import {gql} from "@apollo/client/core";

const RUN_FILE_CRAWL = gql`
  mutation RunFileCrawl($name: String!) {
    runFileCrawl(name: $name) {name}
  }
`

const FileCrawl = () => {
  const selectedFileCrawl = useSelector((state: State) => state.selectedFileCrawl)
  const dispatch = useDispatch();
  const [entryPath, setEntryPath] = useState<string>('');
  const [maxDepth, setMaxDepth] = useState<number | undefined>(undefined);
  const [crawlName, setCrawlName] = useState<string>(selectedFileCrawl || '');
  const [crawlList, setCrawlList] = useState<CrawlFilterItem[]>([]);
  const [nextItemIndex, setNextItemIndex] = useState<number>(1);

  const [gqlRunFileCrawl, {loading}] = useMutation(RUN_FILE_CRAWL, {
    variables: {name: crawlName, settings: {entryPath: entryPath, maxDepth: maxDepth}},
    onCompleted: data => {
      dispatch(setCrawlsView(CrawlsViews.FILE_CRAWLS));
      console.trace(data);
    },
    onError: err => {
      alert(err);
      console.error(err);
    }
  });

  const onCancel = () => {
    dispatch(setCrawlsView(CrawlsViews.FILE_CRAWLS));
  }

  const onRun = async () => {
    await gqlRunFileCrawl();
  }

  const onAdd = (item: CrawlFilterItem) => {
    const newItem = {...item, index: nextItemIndex};
    setNextItemIndex(nextItemIndex + 1);
    const newList = [...crawlList, newItem];
    setCrawlList(newList);
  }

  const onSave = (itemToSave: CrawlFilterItem) => {
   // const newList = crawlList.map(item => item.index === itemToSave.index ? itemToSave : item);
    //setCrawlList(newList);
  }

  const onDelete = (itemToDelete: CrawlFilterItem) => {
    //const newList = crawlList.filter(item => item.index !== itemToDelete.index);
    //setCrawlList(newList);
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
                           defaultValue={crawlName} placeholder={selectedFileCrawl || 'Crawl name'}/>
              </Grid>
              <Grid item xs={10}>
                <TextField fullWidth required id="entryPath" label="Entry Path" value={entryPath}
                           disabled={loading}
                           onChange={e => setEntryPath(e.target.value)}
                           placeholder={"/var/local"}/>
              </Grid>
              <Grid item xs={2}>
                <TextField fullWidth id="max_depth" label="Max depth" type={"number"}
                           disabled={loading}
                           value={maxDepth} onChange={e => setMaxDepth(+e.target.value)}
                           placeholder={"1"}/>
              </Grid>
              <Grid item xs={12}>
                <CrawlFilterItemView onAdd={onAdd} disabled={loading}/>
                {crawlList.map((item) =>
                  <CrawlFilterItemView disabled={loading} key={0} filter={undefined} onSave={onSave}
                                       onDelete={()=>{}}/>)}
              </Grid>
            </Grid>
          </form>
        </Box>
      </Paper>
    </Box>
  )
}

export default FileCrawl;
