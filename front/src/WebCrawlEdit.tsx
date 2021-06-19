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

const UPSERT_WEB_CRAWL = gql`
  mutation UpsertWebCrawl($name: String!, $settings: WebCrawlSettingsInput!, $index: String!) {
    upsertWebCrawl(name: $name, settings: $settings, index: $index) {name}
  }
`

interface CrawlFilterItemWithIndex {
  index: number,
  filter: CrawlFilterItem
}

interface CrawlFilterListWithIndex {
  sequenceNumber: number,
  list: CrawlFilterItemWithIndex[]
}

const buildListwithIndex = (crawlFilters?: CrawlFilterItem[]): CrawlFilterListWithIndex => {
  const crawlFilterList: CrawlFilterListWithIndex = {sequenceNumber: 1, list: []};
  if (crawlFilters) {
    for (let crawlFilter of crawlFilters) {
      delete (crawlFilter as any)['__typename']; //Remove Filter type, as the mutation expect a FilterInput
      const newItem: CrawlFilterItemWithIndex = {index: crawlFilterList.sequenceNumber++, filter: crawlFilter};
      crawlFilterList.list.push(newItem);
    }
  }
  return crawlFilterList;
}

const buildCrawlFilterList = (crawlFiltersWithIndex: CrawlFilterItemWithIndex[]): CrawlFilterItem[] => {
  let list: CrawlFilterItem[] = [];
  for (let crawlFilterWithIndex of crawlFiltersWithIndex) {
    list.push(crawlFilterWithIndex.filter);
  }
  return list;
}

const WebCrawlEdit = () => {
  const dispatch = useDispatch();
  const editWebCrawl = useSelector((state: State) => state.editWebCrawl)

  const [indexName, setIndexName] = useState<string>(editWebCrawl?.indexName || '');
  const [entryUrl, setEntryUrl] = useState<string>(editWebCrawl?.settings?.entryUrl || '');
  const [maxDepth, setMaxDepth] = useState<number | undefined>(editWebCrawl?.settings?.maxDepth);
  const [crawlName, setCrawlName] = useState<string>(editWebCrawl?.crawlName || '');
  const [crawlFilterPolicy, setCrawlFilterPolicy] = useState<CrawlFilterStatus>((editWebCrawl?.settings?.filterPolicy) || CrawlFilterStatus.reject);
  const [crawlFilterList, setCrawlFilterList] = useState<CrawlFilterItem[]>(editWebCrawl?.settings?.filters || []);
  const [crawlFilterListWithIndex] = useState<CrawlFilterListWithIndex>(buildListwithIndex(editWebCrawl?.settings?.filters));

  console.log("crawlFilterList", crawlFilterList);

  const [gqlUpsertWebCrawl, {loading}] = useMutation(UPSERT_WEB_CRAWL, {
    variables: {
      name: crawlName,
      settings: {entryUrl: entryUrl, maxDepth: maxDepth, filters: crawlFilterList, filterPolicy: crawlFilterPolicy},
      index: indexName
    },
    onCompleted: data => {
      dispatch(setCrawlsView(CrawlsViews.WEB_CRAWLS));
      console.trace(data);
    },
    onError: err => {
      alert(err);
      console.error(err);
    }
  });

  const onCancel = () => {
    dispatch(setCrawlsView(CrawlsViews.WEB_CRAWLS));
  }

  const onUpsert = async () => {
    await gqlUpsertWebCrawl();
  }

  const onAdd = (item: CrawlFilterItem) => {
    const newItem: CrawlFilterItemWithIndex = {index: crawlFilterListWithIndex.sequenceNumber++, filter: item};
    crawlFilterListWithIndex.list.push(newItem);
    console.log("crawlFilterListWithIndex.list", crawlFilterListWithIndex.list);
    setCrawlFilterList(buildCrawlFilterList(crawlFilterListWithIndex.list));
  }

  const onSave = (itemToSave: CrawlFilterItem, index: number) => {
    console.log("onSave", index);
    const itemToUpdate = crawlFilterListWithIndex.list.find(item => item.index === index);
    if (itemToUpdate) {
      itemToUpdate.filter = itemToSave;
    }
    setCrawlFilterList(buildCrawlFilterList(crawlFilterListWithIndex.list));
  }

  const onDelete = (indexToDelete: number) => {
    const newList = crawlFilterListWithIndex.list.filter(item => item.index !== indexToDelete);
    crawlFilterListWithIndex.list = newList;
    setCrawlFilterList(buildCrawlFilterList(crawlFilterListWithIndex.list));
  }

  const isEdit = editWebCrawl?.settings !== undefined;
  const title = isEdit ? "Edit Crawl" : "New Crawl";

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
                           placeholder={editWebCrawl?.crawlName || 'Crawl name'}/>
              </Grid>
              <Grid item xs={6}>
                <TextField fullWidth required id="IndexName" label="Index Name" value={indexName}
                           disabled={loading}
                           onChange={e => setIndexName(e.target.value)}
                           placeholder={editWebCrawl?.indexName || 'Index name'}/>
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
              <Grid item xs={10}>
                <CrawlFilterItemView onAdd={onAdd} disabled={loading}/>
                {crawlFilterListWithIndex.list.map((item) =>
                  <CrawlFilterItemView disabled={loading} key={item.index} filter={item.filter}
                                       index={item.index} onSave={onSave} onDelete={onDelete}/>)}
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

export default WebCrawlEdit;
