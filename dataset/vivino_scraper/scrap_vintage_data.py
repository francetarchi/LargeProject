import argparse
import json

import utils.constants as c
from utils.requester import Requester


def get_arguments():
    """Gets arguments from the command line.

    Returns:
        A parser with the input arguments.

    """

    parser = argparse.ArgumentParser(usage='Scraps all vintage data from Vivino.')

    parser.add_argument('output_file', help='Output .json file', type=str)

    parser.add_argument('-start_page', help='Starting page identifier', type=int, default=1)

    return parser.parse_args()


if __name__ == '__main__':
    # Gathers the input arguments and its variables
    args = get_arguments()
    output_file = args.output_file
    start_page = args.start_page

    # Instantiates a wrapper over the `requests` package
    r = Requester(c.BASE_URL)

    # Defines the payload, i.e., filters to be used on the search
    payload = {
        "country_codes[]": "it",
        # "food_ids[]": 20,
        # "grape_ids[]": 3,
        # "grape_filter": "varietal",
        # "min_rating": 3.7,
        # "order_by": "ratings_average",
        # "order": "desc",
        # "price_range_min": 25,
        # "price_range_max": 100,
        # "region_ids[]": 383,
        # "wine_style_ids[]": 98,
        # "wine_type_ids[]": 1,
        # "wine_type_ids[]": 2,
        # "wine_type_ids[]": 3,
        # "wine_type_ids[]": 4,
        # "wine_type_ids[]": 7,
        # "wine_type_ids[]": 24,
    }

    # Performs an initial request to get the number of records (wines)
    res = r.get('explore/explore?', params=payload)
    n_matches = res.json()['explore_vintage']['records_matched']

    print(f'Number of total matches: {n_matches}')

    # Creates a dictionary to hold the data
    data = {}
    data['vintages'] = []

    # Iterates through the amount of possible pages
    for i in range(start_page, max(1, int(n_matches / c.RECORDS_PER_PAGE)) + 1):
        # Adds the page to the payload
        payload['page'] = i

        # Performs the request and scraps the URLs
        res = r.get('explore/explore', params=payload)
        matches = res.json()['explore_vintage']['matches']
        
        print(f'\nPage: {payload["page"]}')
        print(f"    --> num of matches: {len(matches)}")
        
        # printings di controllo
        # print(f"\n------------------------------------ 1st MATCH ------------------------------------")

        # print("STRUCTURE")
        # print(f'match[0].keys(): {matches[0].keys()}')
        # print(f"vintage.keys(): {matches[0]['vintage'].keys()}")
        # print(f"price.keys(): {matches[0]['price'].keys()}")

        # print("VALUES")
        # print(f"matches[0]['vintage']['id']: {matches[0]['vintage']['id']}")
        # print(f"matches[0]['vintage']['seo_name']: {matches[0]['vintage']['seo_name']}")
        # print(f"matches[0]['vintage']['name']: {matches[0]['vintage']['name']}")
        # print(f"matches[0]['vintage']['statistics']: {matches[0]['vintage']['statistics']}")
        # print(f"matches[0]['vintage']['year']: {matches[0]['vintage']['year']}")
        # print(f"matches[0]['vintage']['grapes']: {matches[0]['vintage']['grapes']}")
        # print(f"matches[0]['vintage']['wine']: {matches[0]['vintage']['wine']}")
        # print(f"matches[0]['vintage']['image']: {matches[0]['vintage']['image']}")
        # print(f"matches[0]['vintage']['has_valid_ratings']: {matches[0]['vintage']['has_valid_ratings']}")

        # print("------------------------------------ END MATCH ------------------------------------\n")

        # per il development, togliere il commento quando siamo sicuri della forma in cui vogliamo scaricare i dati
        # exit()

        # Iterates over every match (each match is a vintage+price+prices)
        for match in matches:
            # Gathers the vintage data
            vintage = match['vintage']

            # Popping redundant values
            vintage.pop('seo_name', None)
            vintage.pop('image', None)  ########## forse da commentare ##########
            vintage['wine'] = vintage['wine']['id']
            vintage.pop('grapes', None)                     # tutti gli elementi a null
            vintage.pop('has_valid_ratings', None)          # tutti gli elementi a True
            vintage['statistics'].pop('wine_status', None)  # tutti gli elementi a ""

            # Appends current vintage to the dictionary
            data['vintages'].append(vintage)
        break

    # Opens the output .json file in write mode
    with open(output_file, 'w') as f:
        # Dumps the data to the file
        json.dump(data, f)

    # Closes the file
    f.close()
