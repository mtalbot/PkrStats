using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using Moserware.Skills;
using System.Threading.Tasks;
using Moserware.Skills.TrueSkill;

namespace ProcessPkrLog
{
	class Program
	{
		static void Main(string[] args)
		{
			Dictionary<string, Player> players = new Dictionary<string, Player>();
			Dictionary<string, int> plays = new Dictionary<string, int>();
			StreamReader reader = new StreamReader(args[0]);
			StreamWriter writer = new StreamWriter(args[1], false);
			List<Dictionary<string, List<int>>> Games = new List<Dictionary<string, List<int>>>();

			Dictionary<string, List<int>> currentGame = new Dictionary<string, List<int>>();

			string LastLine = "";

			int c = 0;
			while (!reader.EndOfStream)
			{
				string line = reader.ReadLine().ToLower();
				bool draw = false;
				if (line.StartsWith("="))
				{
					line = line.Substring(1);
					draw = true;
				}

				if (line.Contains("	"))
				{
					line = line.Substring(0, line.IndexOf('	'));
				}

				if (line == "sandy" || line == "bag") { line = "sandbag"; }
				if (line == "will" || line == "will hg") { line = "hg"; }
				if (line == "will g") { line = "billy"; }
				if (line == "mark r") { line = "mark"; }
				if (line == "walking\ndave") { line = "walking dave"; }
				if (line == "bladders") { line = "blads"; }
				if (line == "aussie dan") { line = "dan oz"; }

				if (!string.IsNullOrEmpty(line))
				{
					if (c > 1)
					{
						if (draw)
						{
							c--;
						}
						int pos = c - 1;

						if (!currentGame.ContainsKey(line))
						{
							currentGame.Add(line, new List<int>());
						}
						currentGame[line].Add(pos);

						if (!players.ContainsKey(line))
						{
							players.Add(line, new Player(line));
							plays.Add(line, 1);
						}
						else
						{
							plays[line]++;
						}
					}
				}
				else if (string.IsNullOrEmpty(LastLine))
				{
					c = -1;
					Games.Add(currentGame);
					currentGame = new Dictionary<string, List<int>>();
				}

				c++;
				LastLine = line;
			}

			if (c > 1)
			{
				Games.Add(currentGame);
			}

			string[] playersToCareAbout = plays.Where(pair => pair.Value < (Games.Count / 10)).Select(pair => pair.Key).ToArray();

			IDictionary<Player, Rating> scores = players.Values.ToDictionary(pair => pair, pair => GameInfo.DefaultGameInfo.DefaultRating);

			writer.WriteLine("," + players.Keys.Where(name => !playersToCareAbout.Contains(name)).Aggregate((first, second) => string.IsNullOrEmpty(first) ? second : string.Format("{0}, {1}", first, second)));

			int w = 0;

			foreach (var game in Games)
			{
				w++;

				

				IEnumerable<IDictionary<Player, Rating>> sc = game.
					Where(pair => players.ContainsKey(pair.Key)).
					Select(pair => convert(players[pair.Key], scores[players[pair.Key]], pair.Value.OrderBy(val => val).Skip(1).Select(id => v.ElementAtOrDefault(Convert.ToInt32(id * scale) - 1)).Select(item => item != null ? item : v.First())));
				var newscores = TrueSkillCalculator.CalculateNewRatings<Player>(GameInfo.DefaultGameInfo, sc,
					game.
						Select(pair => pair.Value[0]).
						ToArray());

				foreach (var item in newscores)
				{
					if (players.ContainsKey(item.Key.Id.ToString()))
					{
						scores[item.Key] = item.Value;
					}
				}

				writer.WriteLine(w.ToString() + "," + players.Values.Where(name => !playersToCareAbout.Contains(name.Id)).Select(name => scores[name].ConservativeRating.ToString()).Aggregate((first, second) => string.IsNullOrEmpty(first) ? second : string.Format("{0}, {1}", first, second)));
			}



			foreach (var score in scores.Where(name => !playersToCareAbout.Contains(name.Key.Id)).OrderBy(pair => pair.Value.ConservativeRating))
			{
				Console.WriteLine("{0}: {1}", score.Key.Id, score.Value.ConservativeRating);
			}
			writer.Close();
			reader.Close();
			Console.ReadKey();
		}

		public static IDictionary<Player, Rating> convert(Player p, Rating r, IEnumerable<Rating> repeats)
		{
			Dictionary<Player, Rating> rat = new Dictionary<Player, Rating>();
			rat.Add(p, r);
			if (repeats.Count() > 1)
			{
				foreach (var item in repeats)
				{
					rat.Add(new Player(new Guid().ToString()), item);
				}
			}
			return rat;
		}
	}
}
