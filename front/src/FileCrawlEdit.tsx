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
import {CrawlFilterItem, CrawlFilterStatus} from "./types";
import {useMutation} from "@apollo/client";
import {gql} from "@apollo/client/core";
import CrawlFilterStatusOption from "./components/CrawlFilterStatus";
import CrawFilterListWithKey from "./components/CrawFilterListWithKey";

const UPSERT_FILE_CRAWL = gql`
  mutation UpsertFileCrawl($name: String!, $settings: FileCrawlSettingsInput!, $index: String!) {
    upsertFileCrawl(name: $name, settings: $settings, index: $index) {name}
  }
`

const FileCrawlEdit = () => {
  const dispatch = useDispatch();
  const editFileCrawl = useSelector((state: State) => state.editFileCrawl);
  const [indexName, setIndexName] = useState<string>(editFileCrawl?.indexName || '');
  const [entryPath, setEntryPath] = useState<string>(editFileCrawl?.settings?.entryPath || '');
  const [maxDepth, setMaxDepth] = useState<number | undefined>(editFileCrawl?.settings?.maxDepth);
  const [crawlName, setCrawlName] = useState<string>(editFileCrawl?.crawlName || '');
  const [crawlFilterPolicy, setCrawlFilterPolicy] = useState<CrawlFilterStatus>((editFileCrawl?.settings?.filterPolicy) || CrawlFilterStatus.reject);
  const [crawlFilterList, setCrawlFilterList] = useState<CrawlFilterItem[]>(editFileCrawl?.settings?.filters || []);
  const [crawlFilterListWithKey] = useState<CrawFilterListWithKey>(new CrawFilterListWithKey(editFileCrawl?.settings?.filters));

  const [gqlUpsertCrawl, {loading}] = useMutation(UPSERT_FILE_CRAWL, {
    variables: {
      name: crawlName,
      settings: {entryPath: entryPath, maxDepth: maxDepth, filters: crawlFilterList, filterPolicy: crawlFilterPolicy},
      index: indexName
    },
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

  const onUpsert = async () => {
    await gqlUpsertCrawl();
  }

  const onAdd = (item: CrawlFilterItem) => {
    setCrawlFilterList(crawlFilterListWithKey.add(item));
  }

  const onSave = (itemToSave: CrawlFilterItem, key: number) => {
    console.log("onSave", itemToSave, key);
    setCrawlFilterList(crawlFilterListWithKey.save(itemToSave, key));
  }

  const onDelete = (key: number) => {
    setCrawlFilterList(crawlFilterListWithKey.delete(key));
  }

  const isEdit = editFileCrawl?.settings !== undefined;
  const title = isEdit ? "Edit File Crawl" : "New File Crawl";

  return (
    <Box p={1}>
      <Paper>
        <Box p={2}>
          <Grid container justify={"space-between"} spacing={10}>
            <Grid item>
              <Typography align={"right"} variant={"h5"}>{title}</Typography>
            </Grid>
            <Grid item>
              <Grid container spacing={2}>
                <Grid item>
                  <Button variant={"contained"} color={"secondary"}
                          onClick={onCancel}>Cancel</Button>
                </Grid>
                <Grid item>
                  <Button variant={"contained"} color={"primary"}
                          onClick={onUpsert}>Upsert</Button>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
          <form noValidate autoComplete="off">
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField fullWidth
                           required id="CrawlName" label="Crawl Name" value={crawlName}
                           disabled={true}
                           onChange={e => setCrawlName(e.target.value)}
                           placeholder={editFileCrawl?.crawlName || 'Crawl name'}/>
              </Grid>
              <Grid item xs={6}>
                <TextField fullWidth required id="IndexName" label="Index Name" value={indexName}
                           disabled={loading}
                           onChange={e => setIndexName(e.target.value)}
                           placeholder={editFileCrawl?.indexName || 'Index name'}/>
              </Grid>
              <Grid item xs={10}>
                <TextField fullWidth required id="entryUrl" label="Entry Path" value={entryPath}
                           disabled={loading}
                           onChange={e => setEntryPath(e.target.value)}
                           placeholder={"/var/lib/share"}/>
              </Grid>
              <Grid item xs={2}>
                <TextField fullWidth id="max_depth" label="Max depth" type={"number"}
                           disabled={loading}
                           value={maxDepth} onChange={e => setMaxDepth(+e.target.value)}
                           placeholder={"1"}/>
              </Grid>
              <Grid item xs={10}>
                <CrawlFilterItemView onAdd={onAdd} disabled={loading}/>
                {crawlFilterListWithKey.list.map((item) =>
                  <CrawlFilterItemView disabled={loading} key={item.key} filter={item.filter}
                                       index={item.key} onSave={onSave} onDelete={onDelete}/>)}
              </Grid>
              <Grid item xs={2}>
                <CrawlFilterStatusOption disabled={false} status={crawlFilterPolicy}
                                         setStatus={setCrawlFilterPolicy} isDefault={true}/>
              </Grid>
            </Grid>
          </form>
        </Box>
      </Paper>
    </Box>
  )
}

export default FileCrawlEdit;
